## For testing purpose

namespace java com.facebook.nifty.test

enum ResultCode
{
  OK,
  TRY_LATER
}

struct LogEntry
{
  1:  string category,
  2:  string message
}

service Scribe
{
  ResultCode log(1: list<LogEntry> messages);
}