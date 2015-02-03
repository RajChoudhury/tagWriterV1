package raj.ndefexample;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private NfcAdapter nfcAdapter;

    private void enableForegroundDispatch(){
        Intent intent = new Intent(this, MainActivity.class).
                addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[] {};

        String[] [] techList = new String[] [] {
                { android.nfc.tech.Ndef.class.getName() },
                { android.nfc.tech.NdefFormatable.class.getName() } };
        if (Build.DEVICE.matches(".*generic*.")) {
            techList = null;
        }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techList);
    }

    private boolean formatTag(Tag tag, NdefMessage ndefMessage){
        try {
            NdefFormatable ndefFormat = NdefFormatable.get(tag);

            if (ndefFormat != null) {
                ndefFormat.connect();
                ndefFormat.format(ndefMessage);
                ndefFormat.close();
                return true;
            }
        } catch (Exception e){
            Log.e("formatTag", e.getMessage());
        }
        return false;
    }

    private boolean writeNdefMessage(Tag tag, NdefMessage ndefMessage){
        try {
            if (tag != null){
                Ndef ndef = Ndef.get(tag);
                if (ndef == null){
                    return formatTag(tag, ndefMessage);
                } else {
                    ndef.connect();

                    if (ndef.isWritable()){
                        ndef.writeNdefMessage(ndefMessage);
                        ndef.close();
                        return true;
                    }
                    ndef.close();
                }
            }
        } catch (Exception e) {
            Log.e("formatTag", e.getMessage());
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            if (isNfcIntent(intent)) {
                NdefRecord uriRecord = NdefRecord.createUri("http://www.seacitymuseum.co.uk/");
                NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {uriRecord});
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                boolean writeResult = writeNdefMessage(tag, ndefMessage);

                if (writeResult) {
                    Toast.makeText(this, "Tag Written!",Toast.LENGTH_SHORT).show();
                } else { Toast.makeText(this, "Tag write failed!",Toast.LENGTH_SHORT).show();
            }
        }
        } catch (Exception e) {
            Log.e("onNewIntent", e.getMessage());
        }
        super.onNewIntent(intent);
    }

    boolean isNfcIntent(Intent intent) {
        return intent.hasExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    protected void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume(){
        super.onResume();

        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
