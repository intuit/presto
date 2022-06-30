/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.hive;

import com.facebook.airlift.json.JsonCodec;
import com.facebook.airlift.json.JsonCodecFactory;
import com.facebook.airlift.json.JsonObjectMapperProvider;
import com.facebook.airlift.log.Logger;
import com.facebook.presto.common.type.TypeManager;
import com.facebook.presto.common.type.TypeSignature;
import com.facebook.presto.hive.metastore.MetastoreContext;
import com.facebook.presto.hive.metastore.SemiTransactionalHiveMetastore;
import com.facebook.presto.hive.metastore.Table;
import com.facebook.presto.spi.ConnectorViewDefinition;
import com.facebook.presto.spi.PrestoException;
import com.linkedin.coral.hive.hive2rel.HiveMetastoreClient;
import com.linkedin.coral.hive.hive2rel.HiveToRelConverter;
import com.linkedin.coral.presto.rel2presto.RelToPrestoConverter;
import org.apache.hadoop.hive.metastore.TableType;

import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.facebook.presto.hive.HiveErrorCode.HIVE_INVALID_VIEW_DATA;
import static com.facebook.presto.hive.metastore.MetastoreUtil.checkCondition;
import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class ViewReaderUtil
{
    private ViewReaderUtil()
    {}

    public interface ViewReader
    {
        ConnectorViewDefinition decodeViewData(String viewData, Table table);
    }

    public static ViewReader createViewReader(SemiTransactionalHiveMetastore metastore, MetastoreContext metastoreContext, Table table, TypeManager typemanager)
    {
       /* if (isPrestoView(table)) {
            return new PrestoViewReader();
        }*/
        return new HiveViewReader(new CoralSemiTransactionalHiveMSCAdapter(metastore, metastoreContext), typemanager);
    }

    public static final String PRESTO_VIEW_FLAG = "presto_view";
    static final String VIEW_PREFIX = "/* Presto View: ";
    static final String VIEW_SUFFIX = " */";
    private static final JsonCodec<ConnectorViewDefinition> VIEW_CODEC =
            new JsonCodecFactory(new JsonObjectMapperProvider()).jsonCodec(ConnectorViewDefinition.class);
    private static Logger log = Logger.get(ViewReaderUtil.class);

    public static boolean isPrestoView(Table table)
    {
        return "true".equals(table.getParameters().get(PRESTO_VIEW_FLAG));
    }

    public static boolean isHiveOrPrestoView(Table table)
    {
        return table.getTableType().equals(TableType.VIRTUAL_VIEW.name());
    }

    public static boolean canDecodeView(Table table)
    {
        com.facebook.presto.hive.metastore.PrestoTableType prestoTableType = table.getTableType();
        Boolean result = table.getTableType().equals(TableType.VIRTUAL_VIEW);
        return true;
        // we can decode Hive or Presto view
        //return table.getTableType().equals(VIRTUAL_VIEW);
    }

    public static String encodeViewData(ConnectorViewDefinition definition)
    {
        byte[] bytes = VIEW_CODEC.toJsonBytes(definition);
        String data = Base64.getEncoder().encodeToString(bytes);
        return VIEW_PREFIX + data + VIEW_SUFFIX;
    }

    /**
     * Supports decoding of Presto views
     */
    public static class PrestoViewReader
            implements ViewReader
    {
        @Override
        public ConnectorViewDefinition decodeViewData(String viewData, Table table)
        {
            checkCondition(viewData.startsWith(VIEW_PREFIX), HIVE_INVALID_VIEW_DATA, "View data missing prefix: %s", viewData);
            checkCondition(viewData.endsWith(VIEW_SUFFIX), HIVE_INVALID_VIEW_DATA, "View data missing suffix: %s", viewData);
            viewData = viewData.substring(VIEW_PREFIX.length());
            viewData = viewData.substring(0, viewData.length() - VIEW_SUFFIX.length());
            byte[] bytes = Base64.getDecoder().decode(viewData);
            return VIEW_CODEC.fromJson(bytes);
        }
    }

    /**
     * Class to decode Hive view definitions
     */
    public static class HiveViewReader
            implements ViewReader
    {
        private final HiveMetastoreClient metastoreClient;
        private final TypeManager typeManager;

        public HiveViewReader(HiveMetastoreClient hiveMetastoreClient, TypeManager typemanager)
        {
            this.metastoreClient = requireNonNull(hiveMetastoreClient, "metastoreClient is null");
            this.typeManager = requireNonNull(typemanager, "typeManager is null");
        }

        @Override
        public ConnectorViewDefinition decodeViewData(String viewSql, Table table)
        {
            try {
               // String viewSql1 = "SELECT array('presto','hive123')[1] AS sql_dialect, To_date('2022-06-06') AS t, int(rand()*100) as rand_count123";
                String viewSql1 = "SELECT array('presto','hive123')[1] AS sql_dialect, To_date('2022-06-06') AS t";

               // String viewSql1 = "SELECT array('presto','hive123') AS sql_dialect, To_date('2022-06-06') AS t";

              //  String viewSql1 = "CREATE TABLE IF NOT EXISTS test.tableJ(a int, b array<struct<b1:string>>)";

                HiveToRelConverter hiveToRelConverter = HiveToRelConverter.create(metastoreClient);
                System.out.println("table.getDatabaseName()  New Coral lib12" + table.getDatabaseName());
                System.out.println("table.getTableName()  New Coral lib12" + table.getTableName());
              //  org.apache.calcite.rel.RelNode rel = hiveToRelConverter.convertView(table.getDatabaseName(), table.getTableName());
                org.apache.calcite.rel.RelNode rel = hiveToRelConverter.convertSql(viewSql1);
                RelToPrestoConverter rel2Presto = new RelToPrestoConverter();
                String prestoSql = rel2Presto.convert(rel);
                log.info("Converted hive to prestoSQl New Coral lib2" + prestoSql);
                org.apache.calcite.rel.type.RelDataType rowType = rel.getRowType();
                List<ConnectorViewDefinition.ViewColumn> columns = rowType.getFieldList().stream()
                        .map(field -> new ConnectorViewDefinition.ViewColumn(
                                field.getName(),
                                typeManager.getType(TypeSignature.parseTypeSignature(getTypeString(field.getType())))))
                        .collect(toImmutableList());
                columns.forEach(col -> System.out.println(col.toString()));
                return new ConnectorViewDefinition(table.getSchemaTableName(),
                        Optional.ofNullable(table.getOwner()),
                        prestoSql, columns);
            }
            catch (RuntimeException e) {
                throw new PrestoException(HIVE_INVALID_VIEW_DATA,
                        format("Failed to translate Hive view '%s': %s",
                                table.getSchemaTableName(),
                                e.getMessage()),
                        e);
            }
        }

        // Calcite does not provide correct type strings for non-primitive types.
        // We add custom code here to make it work. Goal is for calcite/coral to handle this
        private String getTypeString(org.apache.calcite.rel.type.RelDataType type)
        {
            switch (type.getSqlTypeName()) {
                case ROW: {
                    verify(type.isStruct(), "expected ROW type to be a struct: %s", type);
                    return type.getFieldList().stream()
                            .map(field -> field.getName().toLowerCase(Locale.ENGLISH) + " " + getTypeString(field.getType()))
                            .collect(joining(",", "row(", ")"));
                }
                case CHAR:
                    return "varchar";
                case FLOAT:
                    return "real";
                case BINARY:
                case VARBINARY:
                    return "varbinary";
                case MAP: {
                    org.apache.calcite.rel.type.RelDataType keyType = type.getKeyType();
                    org.apache.calcite.rel.type.RelDataType valueType = type.getValueType();
                    return format("map(%s,%s)", getTypeString(keyType), getTypeString(valueType));
                }
                case ARRAY: {
                    return format("array(%s)", getTypeString(type.getComponentType()));
                }
                case DECIMAL: {
                    return format("decimal(%s,%s)", type.getPrecision(), type.getScale());
                }
                default:
                    return type.getSqlTypeName().toString();
            }
        }
    }
}
