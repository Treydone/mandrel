include "common.thrift"

namespace java io.mandrel.worker.thrift

service Worker
{
  oneway void create(1: binary definition);
  
  oneway void start(1: i64 id);

  oneway void pause(1: i64 id);

  oneway void kill(1: i64 id);
}