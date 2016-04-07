package com.test.all_examples.employees;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.test.all_examples.R;

import java.util.List;


public class GetOwnerActivity extends Activity {

  private Account mAccount;
  private EmployeeConnector mEmployeeConnector;

  private TextView mOwnerName;
  private TextView mOwnerEmail;
  private TextView mOwnerId;
  private ProgressBar mProgressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_get_owner);


    mOwnerId = (TextView) findViewById(R.id.owner_id);
    mOwnerName = (TextView) findViewById(R.id.owner_name);
    mOwnerEmail = (TextView) findViewById(R.id.owner_email);

    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Retrieve the Clover account
    if (mAccount == null) {
      mAccount = CloverAccount.getAccount(this);

      if (mAccount == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    // Create and Connect to the MerchantConnector
    connect();

    // Get the merchant object
    getMerchant();
  }


  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }

  private void connect() {
    disconnect();
    if (mAccount != null) {
      mEmployeeConnector = new EmployeeConnector(this, mAccount, null);
      mEmployeeConnector.connect();
    }
  }

  private void disconnect() {
    if (mEmployeeConnector != null) {
      mEmployeeConnector.disconnect();
      mEmployeeConnector = null;
    }
  }

  private void getMerchant() {

    new AsyncTask<Void, Void, Employee>() {

      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        // Show progressBar while waiting
        mProgressBar.setVisibility(View.VISIBLE);
      }

      @Override
      protected Employee doInBackground(Void... params) {
        try {
          List<Employee> employees = mEmployeeConnector.getEmployees();
          for (Employee employee : employees) {
            if (employee.getIsOwner()) {
              return employee;
            }
          }
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (ClientException e) {
          e.printStackTrace();
        } catch (ServiceException e) {
          e.printStackTrace();
        } catch (BindingException e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override
      protected void onPostExecute(Employee owner) {
        super.onPostExecute(owner);

        if (!isFinishing()) {
          // Populate the merchant information
          if (owner != null) {
            mOwnerId.setText(owner.getId());
            mOwnerName.setText(owner.getName());
            mOwnerEmail.setText(owner.getEmail());
          }

          // Hide the progressBar
          mProgressBar.setVisibility(View.GONE);
        }
      }
    }.execute();
  }
}
