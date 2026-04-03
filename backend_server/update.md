
# Removed the login screen no need for user auth directly works
# Removed handling the auth header , no neeed to send auth header for any of the requests
# getScanDetails is now removed, no need for the duplicate thingy , keep on polling scanStatus 0 means pending, 1 means assignedd , 2 means the task is done , so when status == 2,  then you can read the other informatin  for the result , else discard and continue polling
# Removed the apk scanning route


