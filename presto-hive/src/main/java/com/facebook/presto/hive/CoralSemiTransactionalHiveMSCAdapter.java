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

import com.facebook.presto.hive.metastore.MetastoreContext;
import com.facebook.presto.hive.metastore.PrincipalPrivileges;
import com.facebook.presto.hive.metastore.SemiTransactionalHiveMetastore;
import com.facebook.presto.hive.metastore.thrift.ThriftMetastoreUtil;
import com.google.common.collect.ImmutableMultimap;
import com.linkedin.coral.hive.hive2rel.HiveMetastoreClient;
import org.apache.hadoop.hive.metastore.api.Database;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class CoralSemiTransactionalHiveMSCAdapter
        implements HiveMetastoreClient
{
    private final SemiTransactionalHiveMetastore delegate;
    private final MetastoreContext metastoreContext;

    public CoralSemiTransactionalHiveMSCAdapter(SemiTransactionalHiveMetastore coralHiveMetastoreClient, MetastoreContext metastoreContext)
    {
        this.delegate = requireNonNull(coralHiveMetastoreClient, "coralHiveMetastoreClient is null");
        this.metastoreContext = metastoreContext;
    }

    @Override
    public List<String> getAllDatabases()
    {
        return delegate.getAllDatabases(metastoreContext);
    }

    // returning null for missing entry is as per Coral's requirements
    @Override
    public Database getDatabase(String dbName)
    {
        return delegate.getDatabase(metastoreContext, dbName).map(ThriftMetastoreUtil::toMetastoreApiDatabase).orElse(null);
    }

    @Override
    public List<String> getAllTables(String dbName)
    {
        return delegate.getAllTables(metastoreContext, dbName).get();
    }

    @Override
    public org.apache.hadoop.hive.metastore.api.Table getTable(String dbName, String tableName)
    {
        return delegate.getTable(metastoreContext, dbName, tableName)
                .map(value -> ThriftMetastoreUtil.toMetastoreApiTable(value, new PrincipalPrivileges(ImmutableMultimap.of(), ImmutableMultimap.of()), metastoreContext.getColumnConverter()))
                .orElse(null);
    }
}
