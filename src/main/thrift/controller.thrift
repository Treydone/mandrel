include "common.thrift"

namespace java io.mandrel.controller.thrift

struct Heartbeat
{
  1:  string node
  2:  string info
}

service Controller
{
  oneway void pulse(1: Heartbeat beat);
  
  oneway void start(1: i64 id);

  oneway void pause(1: i64 id);

  oneway void kill(1: i64 id);
}