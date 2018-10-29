package demo.lakini.com.phonenumberauthenticationdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends Activity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private EditText phoneNumberTxt;
    private String phoneNumber;
    private ProgressDialog progressDialog;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeAutoRetrievalTimeOut(String verificationId) {
                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog);
                }
                notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!");
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("onVerificationCompleted", "onVerificationCompleted:" + credential);
                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog);
                }
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("onVerificationFailed", "onVerificationFailed", e);

                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog);
                }

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException" + e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.e("Exception:", "FirebaseTooManyRequestsException" + e);

                }

                notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!");
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                //for low level version which doesn't do aoto verififcation save the verification code and the token

                Log.d("onCodeSent", "onCodeSent:" + verificationId);
                Log.i("Verification code:", verificationId);
            }
        };
    }

    private void showLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickSms(final View view) {

        phoneNumberTxt = findViewById(R.id.msisdn);
        ccp = findViewById(R.id.ccp);
        String countryCode = ccp.getSelectedCountryCodeWithPlus();
        phoneNumber = phoneNumberTxt.getText().toString();
        phoneNumber = countryCode + phoneNumber;

        Log.d("User Phone Number", phoneNumber);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            startPhoneNumberVerification(phoneNumber);
            showProgressDialog(this, "Sending a verification code", false);
        } else {
            showToast("Please enter a valid number to continue!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();
                            Log.d("Sign in with phone auth", "Success " + user);
                            showMainActivity();
                        } else {
                            notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!");
                        }
                    }
                });
    }

    /**
     * Method to show progress dialog
     *
     * @param mActivity
     * @param message
     * @param isCancelable
     * @return dialog
     */
    public ProgressDialog showProgressDialog(final Context mActivity, final String message, boolean isCancelable) {
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.show();
        progressDialog.setCancelable(isCancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    /**
     * Method to dismiss progress dialog
     *
     * @param progressDialog
     */
    public final void dismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void notifyUserAndRetry(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        showLoginActivity();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLoginActivity();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

