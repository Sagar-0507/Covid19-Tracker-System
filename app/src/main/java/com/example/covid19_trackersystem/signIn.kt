package com.example.covid19_trackersystem
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status


/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
class signIn : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    View.OnClickListener {
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mStatusTextView: TextView? = null
    private var mProgressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Views
        mStatusTextView = findViewById<View>(R.id.status) as TextView

        // Button listeners
        findViewById<View>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<View>(R.id.sign_out_button).setOnClickListener(this)
        findViewById<View>(R.id.disconnect_button).setOnClickListener(this)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        // [END build_client]

        // [START customize_button]
        // Set the dimensions of the sign-in button.
        val signInButton = findViewById<View>(R.id.sign_in_button) as SignInButton
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        // [END customize_button]
    }

    public override fun onStart() {
        super.onStart()
        val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient)
        if (opr.isDone) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in")
            val result = opr.get()
            handleSignInResult(result)
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog()
            opr.setResultCallback { googleSignInResult ->
                hideProgressDialog()
                handleSignInResult(googleSignInResult)
            }
        }
    }

    // [START onActivityResult]
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    // [END onActivityResult]
    // [START handleSignInResult]
    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            mStatusTextView!!.text = getString(R.string.signed_in_fmt, acct.displayName)
            updateUI(true)
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false)
        }
    }

    // [END handleSignInResult]
    // [START signIn]
    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // [END signIn]
    // [START signOut]
    private fun signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
            object : ResultCallback<Status?> {
                override fun onResult(p0: Status) {
                    // [START_EXCLUDE]
                    updateUI(false)
                    // [END_EXCLUDE]
                }
            })
    }

    // [END signOut]
    // [START revokeAccess]
    private fun revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
            object : ResultCallback<Status?> {
                override fun onResult(p0: Status) {
                    // [START_EXCLUDE]
                    updateUI(false)
                    // [END_EXCLUDE]
                }
            })
    }

    // [END revokeAccess]
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
    }

    private fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage(getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.hide()
        }
    }

    private fun updateUI(signedIn: Boolean) {
        if (signedIn) {
            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            findViewById<View>(R.id.sign_out_and_disconnect).visibility = View.VISIBLE
        } else {
            mStatusTextView?.setText(R.string.signed_out)
            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<View>(R.id.sign_out_and_disconnect).visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            R.id.disconnect_button -> revokeAccess()
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
        private const val RC_SIGN_IN = 9001
    }
}