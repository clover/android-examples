package com.clover.example.employeemanagement;

import android.app.Activity;
import android.os.Bundle;
import android.os.IInterface;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.accounts.Account;
import android.content.res.Configuration;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.employees.AccountRole;

public class MainActivity extends Activity implements ServiceConnector.OnServiceConnectedListener, EmployeeConnector.OnActiveEmployeeChangedListener {

    private Account account;
    private EmployeeConnector mEmployeeConnector;
    private AccountRole mRole;
    private TextView permissionView;
    private boolean employeeFacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionView = (TextView) findViewById(R.id.currentUser);
        // Gets starting orientation
        if(getOrientation() == Configuration.ORIENTATION_LANDSCAPE){
            employeeFacing = true;
        } else {
            employeeFacing = false;
        }
    }

    private int getOrientation(){
        return getResources().getConfiguration().orientation;
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Retrieve Clover Account
        if (account == null) {
            account = CloverAccount.getAccount(this);

            if (account == null){
                Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Connect to the EmployeeConnector
        connect();

        // Get the employee's role
        getEmployee();
    }

    @Override
    protected void onPause(){
        disconnect();
        super.onPause();
    }

    private void connect(){
        disconnect();
        if (account != null){
            mEmployeeConnector = new EmployeeConnector(this, account, this);
            mEmployeeConnector.connect();
        }
    }

    private void disconnect() {
        if (mEmployeeConnector != null) {
            mEmployeeConnector.disconnect();
            mEmployeeConnector = null;
        }
    }

    private void getEmployee(){
        mEmployeeConnector.getEmployee(new EmployeeConnector.EmployeeCallback<Employee>() {
            @Override
            public void onServiceSuccess(Employee result, ResultStatus status) {
                super.onServiceSuccess(result, status);
                mRole = result.getRole();
                permissionView.setText("Currently logged in as an " + mRole.toString());
            }
        });
    }

    @Override
    public void onActiveEmployeeChanged(Employee employee) {
        if (employee != null) {
            mRole = employee.getRole();
            permissionView.setText("Currently logged in as a " + mRole.toString());
        }
    }

    @Override
    public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
    }

    @Override
    public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
    }

    public void adminClick(View view){
        if (mRole == AccountRole.ADMIN && employeeFacing) {
            Toast.makeText(this, "You have admin permissions.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have admin permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    public void employeeClick(View view){
        if (mRole == AccountRole.EMPLOYEE && employeeFacing) {
            Toast.makeText(this, "You have employee permissions.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have employee permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    public void managerClick(View view){
        if (mRole == AccountRole.MANAGER && employeeFacing) {
            Toast.makeText(this, "You have manager permissions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have manager permissions", Toast.LENGTH_SHORT).show();
        }
    }
}
