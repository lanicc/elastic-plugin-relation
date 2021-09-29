## Relation概念

例如有4张表a、b、c、d 以a表为主表 a - b: a.id = b.id, 1 - 1 a - c: a.id = c.p_id, 1 - n a - d: a.p_id = c.id, n - 1

构建的relation就是 relation

```json
{
  "name": "a",
  "primaryKey": "id",
  "children": [
    {
      "name": "b",
      "primaryKey": "id",
      "relatedKey": "id",
      "nested": false
    },
    {
      "name": "c",
      "primaryKey": "p_id",
      "relatedKey": "id",
      "nested": true
    },
    {
      "name": "d",
      "primaryKey": "id",
      "relatedKey": "p_id",
      "nested": false
    }
  ]
}
```

不支持n-n，及多层嵌套

## 字段释义

- name: 名称，例如从canal同步数据时可以对应表名
- primaryKey: 主键，对于主表作为doc的_id，对于从表与doc _id关联的键
- relatedKey: 关联的主表键
- nested: 对应es中的array和nested类型
- children: 从表

## ActionRequest

只组合es的ActionRequest，不创造

- `BabyCreateIndexRequest`: 提供创建索引和保存relation的功能，内部包含`IndexReques`和`relation`,会根据`IndexReques`创建用户索引，将`relation`
  保存至索引`baby_indies_relation`中
- `BabyIndexRequest`: 提供根据`relation`配置更新数据的能力
    1. 1 - 1: 使用`UpdateRequest.upsert`更新
    2. 1 - n: 使用脚本更新
    3. n - 1: 使用`UpdateByQueryRequest`更新

#### 1

```java
        UpdateRequest updateRequest=
        new UpdateRequest()
        .index(index)
        .id(keyValue)
        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
        .doc(source)
        .upsert(source);
```

#### 1-1

```java
                updateRequest=
        new UpdateRequest()
        .index(index)
        .id(relationPrimaryValue)
        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
        .doc(map)
        .upsert(map);
```

#### 1-n

```java
                updateRequest=
        new UpdateRequest()
        .index(index)
        .id(relationPrimaryValue)
        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
        .script(script)
        .upsert(upsertMap);
```

脚本

```java
if(ctx._source.%s!=null)
        ctx._source.%s.removeIf(item->String.valueOf(item.%s)=='%s');
        if(ctx._source.%s==null)
        ctx._source.%s=[];
        ctx._source.%s.add(params.p);
```

#### n-1

```java
BoolQueryBuilder boolQueryBuilder=QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery(relateKey,relationPrimaryValue));
        UpdateByQueryRequest updateByQueryRequest=
        new UpdateByQueryRequest(
        new SearchRequest(index)
        .source(new SearchSourceBuilder().query(boolQueryBuilder)
        )
        );
        Map<String, Object> params=new HashMap<>();
        params.put("p",source);
        Script script=new Script(ScriptType.INLINE,DEFAULT_SCRIPT_LANG,String.format("ctx._source.%s = params.p",name),params);
        updateByQueryRequest.setScript(script);
```

## Next

- BabyRemoveKeyRequest: 删除key，支持主表、从表
- BabyDeleteRequest，删除文档，主表或从表
- n-1的情况，需要优化，例如先插入了父文档
- 代理TransportClient，实现对客户端管控
- 研究下flush
