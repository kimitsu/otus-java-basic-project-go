# OTUS Java Basic Project

## Simple Go Client-Server

### Client-side

Simple Swing Interface

* Login Form
  * Fields: Login, Password, Server IP
  * Buttons: Login, Register, exit
  * Sends authentication request to the server, displays message box on error, otherwise proceeds to the Lobby
* Lobby
  * User list
  * Challenge button, Exit button
  * Sends or receives challenges, displays Challenge Windows
* Challenge Windows (Outgoing / Incoming)
  * Display the name of the user who sent the challenge or who you sent the challenge to
  * Displays Accept/Reject buttons for incoming challenges
  * Displays Cancel button for outgoing challenges
  * If the challenge is accepted, proceeds to the Game Window
* Game Window
  * Displays names of the players
  * Displays whose move it is now
  * Displays a go board, number of captured stones
    * Clicking the board plays a move if it's your turn
    * The board is redrawn for both players
  * Displays Pass button (allows to play pass on your turn)
  * Resign button resigns the game immediately
  * After two passes score counting phase starts
    * Clicking stones marks them dead and attributes the territory to the opposing player
    * Clicking dead stones unmarks them
    * Resume button returns back to the game, giving your opponent an opportunity to play first
    * Done button finishes the scoring phase
  * After the game is finished the message with the result is displayed for both players
  * Close button closes the game window and returns back to the lobby

### Server-side

* Accept connections and process messages via socket:
  * Login: respond with login confirmed or error
  * User list: respond with user list
  * Send challenge: send message to target users
  * Accept/Reject challenge: dispatch message to challenger
  * Cancel challenge: dispatch message to the challenged
  * Game info request: responds with the established game context id and player names
  * Play move/pass/resign/mark/done: update board state and send it to both players