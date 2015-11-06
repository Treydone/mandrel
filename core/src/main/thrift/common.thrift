namespace java io.mandrel.common.thrift

struct Node
{
  1:  string id
  2:  string infos
}

service Cluster
{
  Node get();
}