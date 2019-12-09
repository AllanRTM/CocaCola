package com.example.supercookie;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sqip.CardEntry;
import sqip.GooglePay;
import sqip.InAppPaymentsSdk;

import static com.google.firebase.auth.FirebaseAuth.*;

public class CheckoutActivity extends AppCompatActivity {

  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 1;

  private final Handler handler = new Handler(Looper.getMainLooper());

  private GooglePayChargeClient googlePayChargeClient;
  private PaymentsClient paymentsClient;
  private OrderSheet orderSheet;
  //firebase
  private static final int RC_SIGN_IN = 0;
  private FirebaseAuth mAuth;
  private AuthStateListener AutoListener;
  //cocacola
  private ListView listView;
  private ListAdapter listAdapter;
  ArrayList<Product> products = new ArrayList<>();
  Button btnPlaceOrder;
  ArrayList<Product> productOrders = new ArrayList<>();
  ArrayList<String> lOrderItems = new ArrayList<String>();

  //FIREBASE
  List<AuthUI.IdpConfig> providers = Arrays.asList(
          new AuthUI.IdpConfig.EmailBuilder().build(),
          new AuthUI.IdpConfig.AnonymousBuilder().build());


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cookie);

    googlePayChargeClient = (GooglePayChargeClient) getLastCustomNonConfigurationInstance();
    if (googlePayChargeClient == null) {
      googlePayChargeClient = ExampleApplication.createGooglePayChargeClient(this);
    }
    googlePayChargeClient.onActivityCreated(this);

    paymentsClient = Wallet.getPaymentsClient(this,
            new Wallet.WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build());

    orderSheet = new OrderSheet();

    enableGooglePayButton(orderSheet);
    orderSheet.setOnPayWithCardClickListener(this::startCardEntryActivity);
    orderSheet.setOnPayWithGoogleClickListener(this::startGooglePayActivity);

    //firebase
    mAuth = getInstance();
    AutoListener = new AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          Toast.makeText(CheckoutActivity.this, "Usuario Ingresado!!", Toast.LENGTH_SHORT).show();
        } else {
          startActivityForResult(
                  AuthUI.getInstance()
                          .createSignInIntentBuilder()
                          .setAvailableProviders(providers)
                          .setIsSmartLockEnabled(false)
                          .build(),
                  RC_SIGN_IN);
        }
      }
    };

    //COCA COLA
    getProduct();
    listView = (ListView) findViewById(R.id.customListView);
    listAdapter = new ListAdapter(this, products);
    listView.setAdapter(listAdapter);

    btnPlaceOrder = (Button) findViewById(R.id.buyButton);
    btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (InAppPaymentsSdk.INSTANCE.getSquareApplicationId().equals("REPLACE_ME")) {
          showMissingSquareApplicationIdDialog();
        } else {
          showOrderSheet();
        }

      }
    });
    //coca_cola
    /*listView =findViewById(R.id.list1);
    adaptador =new Adapter(this,GetArrayItems());
    listView.setAdapter(adaptador);*/

  }
  private void placeOrder()
  {
    productOrders.clear();
    lOrderItems.clear();

    for(int i=0;i<listAdapter.listProducts.size();i++)
    {
      if(listAdapter.listProducts.get(i).CartQuantity > 0)
      {
        Product products = new Product(
                listAdapter.listProducts.get(i).ProductName
                ,listAdapter.listProducts.get(i).ProductPrice
                ,listAdapter.listProducts.get(i).ProductImage
        );

        products.CartQuantity = listAdapter.listProducts.get(i).CartQuantity;
        productOrders.add(products);

        lOrderItems.add(products.getJsonObject().toString());

      }
    }


    JSONArray jsonArray = new JSONArray(lOrderItems);
    openSummary(jsonArray.toString());



  }

  //COCA COLA
 /* public void openSummary(String orderItems)
  {
    Intent summaryIntent = new Intent(this,Summary.class);
    summaryIntent.putExtra("orderItems",orderItems);
    startActivity(summaryIntent);
  }*/

  public void showMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  public void openSummary(String orderItems)
  {
    Intent summaryIntent = new Intent(this,Summary.class);
    summaryIntent.putExtra("orderItems",orderItems);
    startActivity(summaryIntent);
  }

  public void getProduct() {
    products.add(new Product("Coca-cola en Lata",10.0d,R.drawable.primer));
    products.add(new Product("Coca-cola Mini",10.0d,R.drawable.segunda));
    products.add(new Product("Coca-cola Portatil",10.0d,R.drawable.tercera));
    products.add(new Product("Coca-cola Medio Litro",10.0d,R.drawable.cuarta));
    products.add(new Product("Coca-cola Litro",10.0d,R.drawable.quinta));
    products.add(new Product("Coca-cola 1.1 Litros",10.0d,R.drawable.sexta));
    products.add(new Product("Coca-cola 1.25 Litros",10.0d,R.drawable.septima));
    products.add(new Product("Coca-cola 1.5 Litros",10.0d,R.drawable.octava));
    products.add(new Product("Coca-cola Dos Litros",10.0d,R.drawable.novena));
    products.add(new Product("Coca-cola Dos y medio Litros",10.0d,R.drawable.decima));
    products.add(new Product("Coca-cola Tres Litros",10.0d,R.drawable.eleven));

  }
  //firebase
  protected void onResume() {
    super.onResume();
    mAuth.addAuthStateListener(AutoListener);
  }

  protected void onPause(){
    super.onPause();
    mAuth.removeAuthStateListener(AutoListener);

  }
  public void signout(View view) {
    AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
              public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CheckoutActivity.this, "Saliste Exitosamente!!", Toast.LENGTH_SHORT).show();

              }
            });
    finish();
  }

  @Override
  public GooglePayChargeClient onRetainCustomNonConfigurationInstance() {
    return googlePayChargeClient;
  }

  private void enableGooglePayButton(OrderSheet orderSheet) {
    Task<Boolean> readyToPayTask =
        paymentsClient.isReadyToPay(GooglePay.createIsReadyToPayRequest());
    readyToPayTask.addOnCompleteListener(this,
        (task) -> orderSheet.setPayWithGoogleEnabled(task.isSuccessful()));
  }

  private void startCardEntryActivity() {
    CardEntry.startCardEntryActivity(this);
  }

  private void startGooglePayActivity() {
    TransactionInfo transactionInfo = TransactionInfo.newBuilder()
        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
        .setTotalPrice("1.00")

        .setCurrencyCode("USD")
        .build();

    PaymentDataRequest paymentDataRequest =
        GooglePay.createPaymentDataRequest(ConfigHelper.GOOGLE_PAY_MERCHANT_ID,
            transactionInfo);

    Task<PaymentData> googlePayActivityTask = paymentsClient.loadPaymentData(paymentDataRequest);

    AutoResolveHelper.resolveTask(googlePayActivityTask, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
  }

  private void showMissingSquareApplicationIdDialog() {
    new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
        .setTitle(R.string.missing_application_id_title)
        .setMessage(Html.fromHtml(getString(R.string.missing_application_id_message)))
        .setPositiveButton(android.R.string.ok, (dialog, which) -> showOrderSheet())
        .show();
  }

  private void showOrderSheet() {
    orderSheet.show(CheckoutActivity.this);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    CardEntry.handleActivityResult(data, cardEntryActivityResult -> {
      if (cardEntryActivityResult.isSuccess()) {
        if (!ConfigHelper.serverHostSet()) {
          showServerHostNotSet();
        } else {
          showSuccessCharge();
        }
      } else if (cardEntryActivityResult.isCanceled()) {
        Resources res = getResources();
        int delayMs = res.getInteger(R.integer.card_entry_activity_animation_duration_ms);
        handler.postDelayed(this::showOrderSheet, delayMs);
      }
    });

    if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
      handleGooglePayActivityResult(resultCode, data);
    }
  }

  private void handleGooglePayActivityResult(int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (!ConfigHelper.merchantIdSet()) {
        showMerchantIdNotSet();
        return;
      }
      PaymentData paymentData = PaymentData.getFromIntent(data);
      if (paymentData != null && paymentData.getPaymentMethodToken() != null) {
        String googlePayToken = paymentData.getPaymentMethodToken().getToken();
        googlePayChargeClient.charge(googlePayToken);
      }
    } else {
      // The customer canceled Google Pay or an error happened, show the order sheet again.
      showOrderSheet();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (!isChangingConfigurations()) {
      googlePayChargeClient.cancel();
    }
    googlePayChargeClient.onActivityDestroyed();
  }

  public void showError(String message) {
    showOkDialog(R.string.unsuccessful_order, message);
  }

  public void showSuccessCharge() {
    showOkDialog(R.string.successful_order_title, getString(R.string.successful_order_message));
  }

  public void showServerHostNotSet() {
    showOkDialog(R.string.server_host_not_set_title, Html.fromHtml(getString(R.string.server_host_not_set_message)));
  }

  private void showMerchantIdNotSet() {
    showOkDialog(R.string.merchant_id_not_set_title, Html.fromHtml(getString(R.string.merchant_id_not_set_message)));
  }

  private void showOkDialog(int titleResId, CharSequence message) {
    new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
        .setTitle(titleResId)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .show();
  }

  public void showNetworkErrorRetryPayment(Runnable retry) {
    new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
        .setTitle(R.string.network_failure_title)
        .setMessage(getString(R.string.network_failure))
        .setPositiveButton(R.string.retry, (dialog, which) -> retry.run())
        .show();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    orderSheet.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    orderSheet.onRestoreInstanceState(this, savedInstanceState);
  }
}
