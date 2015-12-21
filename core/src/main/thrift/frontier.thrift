include "common.thrift"

namespace java io.mandrel.frontier.thrift

struct Uri
{
  1:  string reference
}

struct Result
{
  1:  Uri uri
}

service Frontier
{
  oneway void create(1: binary definition);
  
  oneway void start(1: i64 id);

  oneway void pause(1: i64 id);

  oneway void kill(1: i64 id);
  
  Uri take(1: i64 id);
  
  oneway void schedule(1: i64 id, 2: Uri uri);
  
  oneway void scheduleM(1: i64 id, 2: set<Uri> uri);
  
  oneway void finished(1: i64 id, 2: Result result);
  
  oneway void deleted(1: i64 id, 2: Uri uri);
}