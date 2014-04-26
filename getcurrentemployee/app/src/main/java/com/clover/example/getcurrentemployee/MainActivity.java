package com.clover.example.getcurrentemployee;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;


public class MainActivity extends Activity implements ServiceConnector.OnServiceConnectedListener, EmployeeConnector.OnActiveEmployeeChangedListener {
  private String TAG = "GetEmployeeExample";
  private EmployeeConnector mEmployeeConnector;
  private Account account;

  private TextView name;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    name = (TextView) findViewById(R.id.name);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
  }


  @Override
  protected void onPause() {
    Log.v(TAG, "Pausing...");
    disconnect();
    super.onPause();
  }

  @Override
  protected void onResume() {
    Log.v(TAG, "...Resumed.");
    super.onResume();

    // Retrieve the Clover account
    if (account == null) {
      account = CloverAccount.getAccount(this);

      if (account == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    // Create and Connect to the MerchantConnector
    connect();

    // Get the merchant object
    getEmployee();
  }

  private void connect() {
    disconnect();
    Log.v(TAG, "Connecting...");
    if (account != null) {
      Log.v(TAG, "Account is not null");
      mEmployeeConnector = new EmployeeConnector(this, account, this);
      mEmployeeConnector.connect();
    }
  }

  private void disconnect() {
    Log.v(TAG, "Disconnecting...");
    if (mEmployeeConnector != null) {
      mEmployeeConnector.disconnect();
      mEmployeeConnector = null;
    }
  }

  private void getEmployee() {
    // Show progressBar while waiting
    progressBar.setVisibility(View.VISIBLE);

    mEmployeeConnector.getEmployee(new EmployeeConnector.EmployeeCallback<Employee>() {
      @Override
      public void onServiceSuccess(Employee result, ResultStatus status) {
        super.onServiceSuccess(result, status);

        // Hide the progressBar
        progressBar.setVisibility(View.GONE);

        name.setText(result.getName());
      }
    });
  }

  @Override
  public void onActiveEmployeeChanged(Employee employee) {
    Log.v(TAG, "Employee change!");
    if (employee != null) {
      name.setText(employee.getName());
    }
  }

  @Override
  public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {

  }

  @Override
  public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {

  }
}
