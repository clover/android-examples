package com.clover.example.employeemanagement;

import android.app.Activity;
import android.os.Bundle;
import android.os.IInterface;
import android.widget.Toast;
import android.view.View;
import android.accounts.Account;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        connect();

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
            }
        });
    }

    @Override
    public void onActiveEmployeeChanged(Employee employee) {
        if (employee != null) {
            mRole = employee.getRole();
        }
    }

    @Override
    public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
    }

    @Override
    public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
    }

    public void adminClick(View view){
        if (mRole == AccountRole.ADMIN) {
            Toast.makeText(this, "You have admin permissions.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have admin rights.", Toast.LENGTH_SHORT).show();
        }
    }

    public void employeeClick(View view){
        if (mRole == AccountRole.EMPLOYEE) {
            Toast.makeText(this, "You have employee permissions.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have employee permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    public void managerClick(View view){
        if (mRole == AccountRole.MANAGER) {
            Toast.makeText(this, "You have manager permissions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You do not have manager permissions", Toast.LENGTH_SHORT).show();
        }
    }

}
