package ch.frontg8.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import ch.frontg8.R;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;

public class AboutMeActivity extends AppCompatActivity {
    private final static int WHITE = 0xFFFFFFFF;
    private final static int BLACK = 0xFF000000;
    private final static int WIDTH = 400;
    private final static int HEIGHT = 400;
    private String publicKey = null;
    private boolean showAsText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        final TextView textViewMyPublicKey = (TextView) findViewById(R.id.editTextMyPubkey);
        publicKey = new String(LibCrypto.getMyPublicKeyBytes(new KeystoreHandler(this), this));
        textViewMyPublicKey.setText(publicKey);
        textViewMyPublicKey.setVisibility(View.GONE);

        Button buttonCopyToClipboard = (Button) findViewById(R.id.buttonCopyToClipboard);
        final Button buttonShowText = (Button) findViewById(R.id.buttonShowText);
        final ImageView imageView = (ImageView) findViewById(R.id.myImage);

        try {
            Bitmap bitmap = encodeAsBitmap(publicKey);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        buttonCopyToClipboard.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", publicKey);
                clipboard.setPrimaryClip(clip);
            }
        });

        buttonShowText.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                if (!showAsText) {
                    textViewMyPublicKey.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    buttonShowText.setText(R.string.AboutActivity_ButtonShowAsQrCode);
                    showAsText = true;
                } else {
                    textViewMyPublicKey.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    buttonShowText.setText(R.string.AboutActivity_ButtonShowAsText);
                    showAsText = false;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;

        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

}
