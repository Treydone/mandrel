include "common.thrift"

namespace java io.mandrel.controller.thrift

struct Heartbeat
{
  1:  string node
  2:  string info
}

service Management
{
  oneway void pulse(1: Heartbeat beat);
}