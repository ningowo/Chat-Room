# Chat-Room

(This is a program I wrote long time ago, being modified now by rearranging the structure for both server and client, and by adding a thread pool for server to speedup.

2021.2.24

Create two inner classes for client to handle sending and listening function. And 160 lines -> 120 lines, yes!

Modifying server class.

<br>

2021.2.27 

Update server and client using Java NIO and rebuild ChatMessage. 
<br>

Todo: 

replace ChatMessage with json(fastjson, yes!)

add a function to let user get usernames of all online users

try to make it works when not sending message from my PC to my PC

revise the structure of server if have idea later.

might support HTTP


)

**This is a chat room program using Java NIO which can support the chating from large amount of users**
<br>
A user can:
- send message to all user(default).
- send private message to a specific user using "/tell toUsername hereIsContent" (If the aiming user do not exist, will get an error message)
- (list all online users)
- logout

<br>
For lastest version, please use ChatServerNIO, ChatClientNIO and ChatMessage.

