package com.gal.carapp.Users;

import android.content.Context;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.sdsmdg.tastytoast.TastyToast;

public class Users_Backendless implements UserInterface{

    Context context;
    public Users_Backendless(Context context){
        this.context=context;
        Backendless.initApp(context,"416B6103-8B22-FE62-FF3F-85552F2B7800","C132C6C2-343C-478C-8D02-732560112625");
    }

    @Override
    public void addNewUser(String userName, String password) {
        BackendlessUser user = new BackendlessUser();
        user.setProperty("name", userName);
        user.setPassword(password);

        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                TastyToast.makeText(context, "User registered successfully",TastyToast.LENGTH_SHORT,TastyToast.SUCCESS).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                TastyToast.makeText(context,"Error registering new user "+fault.getMessage(), TastyToast.LENGTH_SHORT,TastyToast.ERROR).show();
                Log.e("BACKENDLESS_REG", fault.getDetail());
            }
        });
    }
}
