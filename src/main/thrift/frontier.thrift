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
  oneway void create();
  
  Uri take();
  
  oneway void schedule(1: Uri uri);
  
  oneway void finished(1: Result result);
  
  oneway void deleted(1: Uri uri);
  
  oneway void pause();

  oneway void unpause();

  oneway void terminate();
}