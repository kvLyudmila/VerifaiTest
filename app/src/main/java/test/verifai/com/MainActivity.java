package test.verifai.com;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.verifai.Verifai;
import com.verifai.VerifaiResult;
import com.verifai.VerifaiScanMode;
import com.verifai.events.VerifaiCountryCodesListener;
import com.verifai.events.VerifaiLicenceListener;
import com.verifai.events.VerifaiNeuralModelListener;
import com.verifai.events.VerifaiResultListener;
import com.verifai.events.VerifaiVizListener;
import com.verifai.exceptions.fatal.LicenceNotValidException;
import com.verifai.internal.model.d;
import com.verifai.mrz.MrzData;
import com.verifai.nfc.VerifaiNfcResult;
import com.verifai.viz.VerifaiVizResult;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final String LICENCE = "=== Verifai Licence file V2 ===\n" +
            "G9BL02TeKwQpiJnvyn/skOSxeSZ5DOLDKZwhx7fMTt30kZ80wdaT4PtTvg99SP6LlrMUbK9Q/rvk\n" +
            "DRZON08OxuNBD93fEc6qp9bFfXBJnJO/kMr190Eg/OYeeK/rNo8vNJN0X5uy+ZmpS78BC6XSoas2\n" +
            "EaOloV2I1cqk4HDy1J6Ya1o3RdqeAgdykhRqU/c0PVwLZa/IW8maSq6FwZJJhdoGqruvVimzHzdH\n" +
            "AldB2AswPJ/7EcFrAr2ZkrfyN2krrkMxCjzQm+q2EYlYKQKndQHq5wFCc9/vspLX+tYxeSrqT+xU\n" +
            "k9uiFCyKS40PZN4TSet1wL6348Ub8PO0xObZ3BG1CiEjm5giavV9R68FKYzsQkm8Hw2qtBFVeY1V\n" +
            "7yFG6gfpIh1uTY0iRdmDanOGWp/iRgyk1rm93unc6xgcTd/Sc4pNHVrb5wO2EO4/y4GXSq0yNiRz\n" +
            "35o2iEBrxjRr9shjf3rGWCSc17MQ0VAtEplKEvN963lHaSTfD46oVKCDAO7BJricHI7g7ZIUNLy2\n" +
            "LG7/EF3cJe9OXHLyHq5FR6k8AX2eCeUc9ivpNTkfv1H+z0PPPxiOBcHcgiNFeY6f2gdcl/vEIQJk\n" +
            "thFRwqyHZSUlILSBAsCAukfGaRHDFYGY0IowooTVjlzn0FSNTbSv34TiJjmjahJQFpD93aX4h2i0\n" +
            "8UtW93qFr9KVTz0VmidK+JeoLPr9T4EjkIFM5G7FFgL86PWqDPnrKVoPqBNoFGRdPjatg5y+jntR\n" +
            "s1NuXNr+99Mq/Gk63WzlIps9ou/DSOfw6Y6l70XSRhSicHCneIjRX12VtInUbA==";
    private static final String TAG_VERIFAI = "VERIFAI";
    ArrayList<String> countryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Verifai.configure(LICENCE, "test.verifai.com", new VerifaiLicenceListener() {
            @Override
            public void onConfigured() {
                Log.i(TAG_VERIFAI, "licence is true");
            }

            @Override
            public void onError(@NotNull LicenceNotValidException e) {
                Log.i(TAG_VERIFAI, "licence is false");
            }
        });

        downloadCountries();
        downloadNNModel();

        Verifai.start(this, VerifaiScanMode.AI, new VerifaiResultListener() {
            @Override
            public void onResult(@NotNull VerifaiResult verifaiResult) {
                if (verifaiResult == null) return;
                MrzData mrzData = verifaiResult.getMrzData();
                if (mrzData == null) {
                    Toast.makeText(MainActivity.this, "MRzData is null", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.title)).setText("MRzData is null");
                    ((TextView) findViewById(R.id.txtCountry)).setText("country: ".concat("is absent"));
                    ((TextView) findViewById(R.id.txtSurname)).setText("surname: ".concat("is absent"));
                    ((TextView) findViewById(R.id.txtName)).setText("name: ".concat("is absent"));
                    ((TextView) findViewById(R.id.txtPassportNumber)).setText("passport number: ".concat("is absent"));
                    ((TextView) findViewById(R.id.txtMrz)).setText("MRZData: ".concat("is absent"));
                } else {
                    ((TextView) findViewById(R.id.title)).setText("The Document was be read");
                    ((TextView) findViewById(R.id.txtCountry)).setText("country: ".concat(mrzData.getIssuingCountry()));
                    ((TextView) findViewById(R.id.txtSurname)).setText("surname: ".concat(mrzData.getSurname()));
                    ((TextView) findViewById(R.id.txtName)).setText("name: ".concat(mrzData.getNames()));
                    ((TextView) findViewById(R.id.txtPassportNumber)).setText("passport number: ".concat(mrzData.getDocumentNumber()));
                    ((TextView) findViewById(R.id.txtMrz)).setText("MRZData: ".concat(mrzData.getRaw()));
                }
                Log.i(TAG_VERIFAI, "verifaiResult");
            }

            @Override
            public void onUpdateAvailable() {
                Log.i(TAG_VERIFAI, "onUpdateAvailable");
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                Log.i(TAG_VERIFAI, "onError");
            }
        });

    }

    private void downloadCountries() {
        if (Verifai.isAiModeSupported()) {
            Verifai.downloadCountryCodes(new VerifaiCountryCodesListener() {
                @Override
                public void onDownloaded(@NotNull List<String> list) {
                    countryList = new ArrayList<>();
                    countryList.addAll(list);
                    Log.i(TAG_VERIFAI, "country list is completed");
                }

                @Override
                public void onError(@NotNull Throwable throwable) {
                    Log.i(TAG_VERIFAI, "country list is failed");
                }
            });

        } else {
            // AI mode is not supported
        }
    }

    private void downloadNNModel() {
        Verifai.downloadNeuralModel(MainActivity.this, countryList, null, verifaiNNModelListener);
    }

    VerifaiNeuralModelListener verifaiNNModelListener = new VerifaiNeuralModelListener() {
        @Override
        public void onProgress(int i) {
            Log.i(TAG_VERIFAI, "onProgress download NN");
        }

        @Override
        public void onInitialized() {
            Log.i(TAG_VERIFAI, "onInitialized NN model");
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.i(TAG_VERIFAI, "onError NN model");
        }

        @Override
        public void onSlowDownload() {
            Log.i(TAG_VERIFAI, "onSlowDownload NN Model");
        }
    };
}
