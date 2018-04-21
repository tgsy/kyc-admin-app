# Blocktrace Administrative Application

An android application that allows blocktrace administrative members to verify the identity of new blocktrace customers, who have submitted their personal identication details through the blocktrace user application. 

## Features
There are three main features of this application.

### Identity Verification
Identification details submitted by newly registered customers are retrieved from the Firebase database so that it may be verified against the uploaded image of the user's official identification documents. Verifying the user sends a HTTP request to the backend that checks the validity of the registered user's inputs, such as checking for the validity of the user's IC. If the HTTP requests returns a success, the encrypted user information would be uploaded to the blockchain and all user information stored in Firebase Database would be deleted.

Should the identification details of the customer not pass the checks (human verification & automated checking in the backend server), the identification details submitted by the customer would be rejected and the customer would be prompted to resubmit their identification information when they next login to their own application.

### Token generation
A success in user identification proceeds immediately to generating a new (NFC) token for the newly verified user, which will store the information needed to serve as the user's physical token of identification, namely
1. Private Key
2. Block ID
3. Merkle Raw
4. AES Key

Thereafter, the physical token would be delivered to the customer.

### Restore Token Usability
Existing blocktrace customers who have reported loss of tokens but managed to find their token within a designated period of time after reporting loss (ie. 14 days) can contact the Blocktrace administrative staff to reactivate their token. The administrative user does so by sending another HTTP request to the backend server with the customer's ID.

## Database

### Backend server
Communications with the backend server is done mainly using HTTP requests

### Firebase 
Firebase authentication is used for the administrative user to login
Firebase storage is used to retrieve the customer's uploaded image for verification
Firebase database is used to retrieve the customer's information for verification
