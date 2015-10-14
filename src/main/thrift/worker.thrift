include "common.thrift"

namespace java io.mandrel.worker.thrift

service Frontier
{
  oneway void pause();

  oneway void unpause();

  oneway void terminate();
}