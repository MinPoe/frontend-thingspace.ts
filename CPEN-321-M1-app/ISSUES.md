# M1

## List of issues

### Issue 1: Delete account button is incorrect. 

**Description**:Rather than deleting the account and associated info when the delete account button is clicked, it simply logs the user out by undoing the authentication. 

**How it was fixed?**: To fix this, I called the delete account api on the backend. I did @DELETE("user/profile"), then made a function that calls it. Then I call that function in authViewModel, then logout after that. 

### Issue 2: Immutable Bio

**Description**:The bio in the user profile cannot be changed after account creation. This shouldn't be the case. 

**How it was fixed?**: In ManagedProfileScreen.kt, the bio text field was marked as readOnly=True. In addition, the focus property: canFocus was set to false. This make it so that the text field cannot be interacted with. By changing both of these, the bio can now be updated. 

### Issue 3: Missing logout button

**Description**:Users can never log out since there's no logout button (assuming we change the delete button to actually delete the account)

**How it was fixed?**: I essentially just copied everything that the delete button had, and made a logout equivalent. There was a lot to change, but it was mostly 1:1