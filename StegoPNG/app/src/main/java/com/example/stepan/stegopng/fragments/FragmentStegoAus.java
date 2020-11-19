package com.example.stepan.stegopng.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stepan.stegopng.MersenneTwister;
import com.example.stepan.stegopng.R;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import at.markushi.ui.CircleButton;

import static android.app.Activity.RESULT_OK;


public class FragmentStegoAus extends Fragment implements View.OnClickListener {
    View rootView;
    CircleButton Circlebutton_stego_aus;

    TextView textView4;
    String OpenKEY = "file_for_OpenKEY";
    String open_key_to_hash;

    MersenneTwister rand_gamma;

    Set<Integer> arraySet_gamma;
    Set<Integer> arraySet_gamma2;

    int[] numbers;
    int[] numbers2;
    String str_12_bit;
    String str_message;

    Uri imageUri = null;
    InputStream imageStream = null;
    Bitmap selectedImage = null;
    final int Pick_image = 1;
    String path_for_image;
    int ImageWidth;
    int ImageHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stego_aus, container, false);

        textView4 = rootView.findViewById(R.id.textView4);

        Circlebutton_stego_aus = rootView.findViewById(R.id.Circlebutton_stego_aus);
        Circlebutton_stego_aus.setOnClickListener(this);

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Pick_image);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        getActivity().setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case Pick_image:
                if(resultCode == RESULT_OK) {
                    try {

                        imageUri = imageReturnedIntent.getData();
                        imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);

                        path_for_image = getRealPathFromURI(imageUri);

                        imageStream = null;
                        imageUri = null;

                        ImageWidth = selectedImage.getWidth();
                        ImageHeight = selectedImage.getHeight();
                        /*imageView.setImageBitmap(selectedImage);*/
                        /*Save savefile = new Save();
                        savefile.SaveImage(this,selectedImage);*/


                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    showToast("Контейнер не выбран");
                    Circlebutton_stego_aus.setClickable(false);
                }
        }
    }


    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void showToast(String string)
    {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Circlebutton_stego_aus:

                read_open_key();
                Generate_12_numbers();
                try {
                    read_12_bit(selectedImage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Generate_numbers_for_pixels();
                try {
                    read_message(selectedImage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                to_text();
                textView4.setText(result);
                Toast.makeText(getActivity(), "Сообщение извлечено", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    void read_open_key() {
        open_key_to_hash = "";
        try {
            BufferedReader br_for_open_text = new BufferedReader(new InputStreamReader(getActivity().openFileInput(OpenKEY)));
            String str_for_open_text = "";
            while ((str_for_open_text = br_for_open_text.readLine()) != null) {
                open_key_to_hash = open_key_to_hash + str_for_open_text;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String hashPassword(String password, int len) {

        SHA3.DigestSHA3 md = new SHA3.DigestSHA3(len);
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return Hex.toHexString(digest);
    }

    void Erst_Generation()
    {
        rand_gamma = new MersenneTwister();
        String hash_mersenne = hashPassword(open_key_to_hash, 512);
        int te = hash_mersenne.length();
        long[] mass_te = new long[te];
        for (int i = 0; i < hash_mersenne.length(); i++) {
            char character = hash_mersenne.charAt(i);
            int ascii = (int) character;
            mass_te[i] = (long) ascii;
        }
        rand_gamma.init_by_array(mass_te, te);
    }

    /////////////////////////////////
    String res;
    /////////////////////////////////
    void Generate_12_numbers()
    {
        res = "";
        numbers = new int[12];
        long mers = 429496729;
        long koeff = (mers / (ImageWidth * ImageHeight) + 200) * 10;
        arraySet_gamma = new HashSet<Integer>();
        Erst_Generation();
        int j = 0;
        do {
            numbers[j] = (int) (rand_gamma.genrand_int32() / koeff);
            int i_k = 0;
            while(i_k == 0)
            {
                if (arraySet_gamma.contains(numbers[j]))
                {
                    numbers[j] = (int) (rand_gamma.genrand_int32() / koeff);
                    i_k = 0;
                }
                if (!arraySet_gamma.contains(numbers[j]))
                {
                    arraySet_gamma.add(numbers[j]);
                    res += " " + Integer.toString(numbers[j]);
                    i_k = 1;
                }
            }
            j++;
        } while(j < 12);
    }



    void read_12_bit (Bitmap src) throws InterruptedException {
        str_12_bit = "";
        for (int i = 0; i < numbers.length; i++)
        {
            int x = numbers[i] % src.getWidth();
            int y = numbers[i] / src.getWidth();
            // получим пиксель для корректировки
            int pixelColor = src.getPixel(x, y);
            // получим цвет пикселя для корректировки
            int pixelBlue = Color.blue(pixelColor);

            String str_bin_blue = Integer.toBinaryString(pixelBlue);
            while (str_bin_blue.length() < 8)
            {
                str_bin_blue = "0" + str_bin_blue;
            } // длина точно 8;

            str_12_bit += str_bin_blue.substring(str_bin_blue.length() - 1);
        }
    }



    void Generate_numbers_for_pixels()
    {
        res = "";
        int dlina = Integer.parseInt(str_12_bit, 2);
        numbers2 = new int[12 + dlina];
        long mers = 429496729;
        long koeff = (mers / (ImageWidth * ImageHeight) + 200) * 10;
        arraySet_gamma2 = new HashSet<Integer>();
        Erst_Generation();
        int j = 0;
        do {
            numbers2[j] = (int) (rand_gamma.genrand_int32() / koeff);
            int i_k = 0;
            while(i_k == 0)
            {
                if (arraySet_gamma2.contains(numbers2[j]))
                {
                    numbers2[j] = (int) (rand_gamma.genrand_int32() / koeff);
                    i_k = 0;
                }
                if (!arraySet_gamma2.contains(numbers2[j]))
                {
                    arraySet_gamma2.add(numbers2[j]);
                    res += " " + Integer.toString(numbers2[j]);
                    i_k = 1;
                }
            }
            j++;
        } while(j < 12 + dlina);
    }

    void read_message (Bitmap src) throws InterruptedException {
        str_message = "";
        for (int i = 12; i < numbers2.length; i++)
        {
            int x = numbers2[i] % src.getWidth();
            int y = numbers2[i] / src.getWidth();
            // получим пиксель для корректировки
            int pixelColor = src.getPixel(x, y);
            // получим цвет пикселя для корректировки
            int pixelBlue = Color.blue(pixelColor);

            String str_bin_blue = Integer.toBinaryString(pixelBlue);
            while (str_bin_blue.length() < 8)
            {
                str_bin_blue = "0" + str_bin_blue;
            } // длина точно 8;

            str_message += str_bin_blue.substring(str_bin_blue.length() - 1);
        }
    }


    String result;
    String aus = "";

    public void to_text() {

            result = "";
            String arrStr[] = str_message.split("");
            int count = 0;
            for (int j = 0; j < arrStr.length; j++) {
                if (j > 0) {
                    //aus = "";
                    aus = aus + arrStr[j];
                    count++;
                    if (count % 8 == 0) {

                        if (aus.compareTo("00000000") == 0) {
                            result = result + "а";
                        }
                        if (aus.compareTo("00000001") == 0) {
                            result = result + "б";
                        }
                        if (aus.compareTo("00000010") == 0) {
                            result = result + "в";
                        }
                        if (aus.compareTo("00000011") == 0) {
                            result = result + "г";
                        }
                        if (aus.compareTo("00000100") == 0) {
                            result = result + "д";
                        }
                        if (aus.compareTo("00000101") == 0) {
                            result = result + "е";
                        }
                        if (aus.compareTo("00000110") == 0) {
                            result = result + "ё";
                        }
                        if (aus.compareTo("00000111") == 0) {
                            result = result + "ж";
                        }
                        if (aus.compareTo("00001000") == 0) {
                            result = result + "з";
                        }
                        if (aus.compareTo("00001001") == 0) {
                            result = result + "и";
                        }
                        if (aus.compareTo("00001010") == 0) {
                            result = result + "й";
                        }
                        if (aus.compareTo("00001011") == 0) {
                            result = result + "к";
                        }
                        if (aus.compareTo("00001100") == 0) {
                            result = result + "л";
                        }
                        if (aus.compareTo("00001101") == 0) {
                            result = result + "м";
                        }
                        if (aus.compareTo("00001110") == 0) {
                            result = result + "н";
                        }
                        if (aus.compareTo("00001111") == 0) {
                            result = result + "о";
                        }
                        if (aus.compareTo("00010000") == 0) {
                            result = result + "п";
                        }
                        if (aus.compareTo("00010001") == 0) {
                            result = result + "р";
                        }
                        if (aus.compareTo("00010010") == 0) {
                            result = result + "с";
                        }
                        if (aus.compareTo("00010011") == 0) {
                            result = result + "т";
                        }
                        if (aus.compareTo("00010100") == 0) {
                            result = result + "у";
                        }
                        if (aus.compareTo("00010101") == 0) {
                            result = result + "ф";
                        }
                        if (aus.compareTo("00010110") == 0) {
                            result = result + "х";
                        }
                        if (aus.compareTo("00010111") == 0) {
                            result = result + "ц";
                        }
                        if (aus.compareTo("00011000") == 0) {
                            result = result + "ч";
                        }
                        if (aus.compareTo("00011001") == 0) {
                            result = result + "ш";
                        }
                        if (aus.compareTo("00011010") == 0) {
                            result = result + "щ";
                        }
                        if (aus.compareTo("00011011") == 0) {
                            result = result + "ъ";
                        }
                        if (aus.compareTo("00011100") == 0) {
                            result = result + "ы";
                        }
                        if (aus.compareTo("00011101") == 0) {
                            result = result + "ь";
                        }
                        if (aus.compareTo("00011110") == 0) {
                            result = result + "э";
                        }
                        if (aus.compareTo("00011111") == 0) {
                            result = result + "ю";
                        }
                        if (aus.compareTo("00100000") == 0) {
                            result = result + "я";
                        }
                        if (aus.compareTo("00100001") == 0) {
                            result = result + "А";
                        }
                        if (aus.compareTo("00100010") == 0) {
                            result = result + "Б";
                        }
                        if (aus.compareTo("00100011") == 0) {
                            result = result + "В";
                        }
                        if (aus.compareTo("00100100") == 0) {
                            result = result + "Г";
                        }
                        if (aus.compareTo("00100101") == 0) {
                            result = result + "Д";
                        }
                        if (aus.compareTo("00100110") == 0) {
                            result = result + "Е";
                        }
                        if (aus.compareTo("00100111") == 0) {
                            result = result + "Ё";
                        }
                        if (aus.compareTo("00101000") == 0) {
                            result = result + "Ж";
                        }
                        if (aus.compareTo("00101001") == 0) {
                            result = result + "З";
                        }
                        if (aus.compareTo("00101010") == 0) {
                            result = result + "И";
                        }
                        if (aus.compareTo("00101011") == 0) {
                            result = result + "Й";
                        }
                        if (aus.compareTo("00101100") == 0) {
                            result = result + "К";
                        }
                        if (aus.compareTo("00101101") == 0) {
                            result = result + "Л";
                        }
                        if (aus.compareTo("00101110") == 0) {
                            result = result + "М";
                        }
                        if (aus.compareTo("00101111") == 0) {
                            result = result + "Н";
                        }
                        if (aus.compareTo("00110000") == 0) {
                            result = result + "О";
                        }
                        if (aus.compareTo("00110001") == 0) {
                            result = result + "П";
                        }
                        if (aus.compareTo("00110010") == 0) {
                            result = result + "Р";
                        }
                        if (aus.compareTo("00110011") == 0) {
                            result = result + "С";
                        }
                        if (aus.compareTo("00110100") == 0) {
                            result = result + "Т";
                        }
                        if (aus.compareTo("00110101") == 0) {
                            result = result + "У";
                        }
                        if (aus.compareTo("00110110") == 0) {
                            result = result + "Ф";
                        }
                        if (aus.compareTo("00110111") == 0) {
                            result = result + "Х";
                        }
                        if (aus.compareTo("00111000") == 0) {
                            result = result + "Ц";
                        }
                        if (aus.compareTo("00111001") == 0) {
                            result = result + "Ч";
                        }
                        if (aus.compareTo("00111010") == 0) {
                            result = result + "Ш";
                        }
                        if (aus.compareTo("00111011") == 0) {
                            result = result + "Щ";
                        }
                        if (aus.compareTo("00111100") == 0) {
                            result = result + "Ъ";
                        }
                        if (aus.compareTo("00111101") == 0) {
                            result = result + "Ы";
                        }
                        if (aus.compareTo("00111110") == 0) {
                            result = result + "Ь";
                        }
                        if (aus.compareTo("00111111") == 0) {
                            result = result + "Э";
                        }
                        if (aus.compareTo("01000000") == 0) {
                            result = result + "Ю";
                        }
                        if (aus.compareTo("01000001") == 0) {
                            result = result + "Я";
                        }
                        if (aus.compareTo("01000010") == 0) {
                            result = result + " ";
                        }
                        if (aus.compareTo("01000011") == 0) {
                            result = result + "=";
                        }
                        if (aus.compareTo("01000100") == 0) {
                            result = result + ",";
                        }
                        if (aus.compareTo("01000101") == 0) {
                            result = result + ".";
                        }
                        if (aus.compareTo("01000110") == 0) {
                            result = result + "!";
                        }
                        if (aus.compareTo("01000111") == 0) {
                            result = result + "?";
                        }
                        if (aus.compareTo("01001000") == 0) {
                            result = result + ":";
                        }
                        if (aus.compareTo("01001001") == 0) {
                            result = result + "'";
                        }
                        if (aus.compareTo("01001010") == 0) {
                            result = result + "-";
                        }
                        if (aus.compareTo("01001011") == 0) {
                            result = result + "0";
                        }
                        if (aus.compareTo("01001100") == 0) {
                            result = result + "1";
                        }
                        if (aus.compareTo("01001101") == 0) {
                            result = result + "2";
                        }
                        if (aus.compareTo("01001110") == 0) {
                            result = result + "3";
                        }
                        if (aus.compareTo("01001111") == 0) {
                            result = result + "4";
                        }
                        if (aus.compareTo("01010000") == 0) {
                            result = result + "5";
                        }
                        if (aus.compareTo("01010001") == 0) {
                            result = result + "6";
                        }
                        if (aus.compareTo("01010010") == 0) {
                            result = result + "7";
                        }
                        if (aus.compareTo("01010011") == 0) {
                            result = result + "8";
                        }
                        if (aus.compareTo("01010100") == 0) {
                            result = result + "9";
                        }
                        if (aus.compareTo("01010101") == 0) {
                            result = result + ")";
                        }
                        if (aus.compareTo("01010110") == 0) {
                            result = result + "(";
                        }
                        if (aus.compareTo("01010111") == 0) {
                            result = result + "*";
                        }
                        if (aus.compareTo("01011000") == 0) {
                            result = result + ";";
                        }
                        if (aus.compareTo("01011001") == 0) {
                            result = result + "&";
                        }
                        if (aus.compareTo("01011010") == 0) {
                            result = result + "@";
                        }
                        if (aus.compareTo("01011011") == 0) {
                            result = result + "%";
                        }
                        if (aus.compareTo("01011100") == 0) {
                            result = result + "$";
                        }
                        if (aus.compareTo("01011101") == 0) {
                            result = result + "+";
                        }
                        if (aus.compareTo("01011110") == 0) {
                            result = result + "#";
                        }
                        if (aus.compareTo("01011111") == 0) {
                            result = result + "\"";
                        }
                        if (aus.compareTo("01100000") == 0) {
                            result = result + "a";
                        }
                        if (aus.compareTo("01100001") == 0) {
                            result = result + "b";
                        }
                        if (aus.compareTo("01100010") == 0) {
                            result = result + "c";
                        }
                        if (aus.compareTo("01100011") == 0) {
                            result = result + "d";
                        }
                        if (aus.compareTo("01100100") == 0) {
                            result = result + "e";
                        }
                        if (aus.compareTo("01100101") == 0) {
                            result = result + "f";
                        }
                        if (aus.compareTo("01100110") == 0) {
                            result = result + "g";
                        }
                        if (aus.compareTo("01100111") == 0) {
                            result = result + "h";
                        }
                        if (aus.compareTo("01101000") == 0) {
                            result = result + "i";
                        }
                        if (aus.compareTo("01101001") == 0) {
                            result = result + "j";
                        }
                        if (aus.compareTo("01101010") == 0) {
                            result = result + "k";
                        }
                        if (aus.compareTo("01101011") == 0) {
                            result = result + "l";
                        }
                        if (aus.compareTo("01101100") == 0) {
                            result = result + "m";
                        }
                        if (aus.compareTo("01101101") == 0) {
                            result = result + "n";
                        }
                        if (aus.compareTo("01101110") == 0) {
                            result = result + "o";
                        }
                        if (aus.compareTo("01101111") == 0) {
                            result = result + "p";
                        }
                        if (aus.compareTo("01110000") == 0) {
                            result = result + "q";
                        }
                        if (aus.compareTo("01110001") == 0) {
                            result = result + "r";
                        }
                        if (aus.compareTo("01110010") == 0) {
                            result = result + "s";
                        }
                        if (aus.compareTo("01110011") == 0) {
                            result = result + "t";
                        }
                        if (aus.compareTo("01110100") == 0) {
                            result = result + "u";
                        }
                        if (aus.compareTo("01110101") == 0) {
                            result = result + "v";
                        }
                        if (aus.compareTo("01110110") == 0) {
                            result = result + "w";
                        }
                        if (aus.compareTo("01110111") == 0) {
                            result = result + "x";
                        }
                        if (aus.compareTo("01111000") == 0) {
                            result = result + "y";
                        }
                        if (aus.compareTo("01111001") == 0) {
                            result = result + "z";
                        }
                        if (aus.compareTo("01111010") == 0) {
                            result = result + "ﺎ";
                        }
                        if (aus.compareTo("01111011") == 0) {
                            result = result + "ﺍ";
                        }
                        if (aus.compareTo("01111100") == 0) {
                            result = result + "ﺐ";
                        }
                        if (aus.compareTo("01111101") == 0) {
                            result = result + "ﺒ";
                        }
                        if (aus.compareTo("01111110") == 0) {
                            result = result + "ﺑ";
                        }
                        if (aus.compareTo("01111111") == 0) {
                            result = result + "ﺏ";
                        }
                        if (aus.compareTo("10000000") == 0) {
                            result = result + "ﺖ";
                        }
                        if (aus.compareTo("10000001") == 0) {
                            result = result + "ﺘ";
                        }
                        if (aus.compareTo("10000010") == 0) {
                            result = result + "ﺗ";
                        }
                        if (aus.compareTo("10000011") == 0) {
                            result = result + "ﺕ";
                        }
                        if (aus.compareTo("10000100") == 0) {
                            result = result + "ﺚ";
                        }
                        if (aus.compareTo("10000101") == 0) {
                            result = result + "ﺜ";
                        }
                        if (aus.compareTo("10000110") == 0) {
                            result = result + "ﺛ";
                        }
                        if (aus.compareTo("10000111") == 0) {
                            result = result + "ﺙ";
                        }
                        if (aus.compareTo("10001000") == 0) {
                            result = result + "ﺞ";
                        }
                        if (aus.compareTo("10001001") == 0) {
                            result = result + "ﺠ";
                        }
                        if (aus.compareTo("10001010") == 0) {
                            result = result + "ﺟ";
                        }
                        if (aus.compareTo("10001011") == 0) {
                            result = result + "ﺝ";
                        }
                        if (aus.compareTo("10001100") == 0) {
                            result = result + "ﺢ";
                        }
                        if (aus.compareTo("10001101") == 0) {
                            result = result + "ﺤ";
                        }
                        if (aus.compareTo("10001110") == 0) {
                            result = result + "ﺣ";
                        }
                        if (aus.compareTo("10001111") == 0) {
                            result = result + "ﺡ";
                        }
                        if (aus.compareTo("10010000") == 0) {
                            result = result + "ﺦ";
                        }
                        if (aus.compareTo("10010001") == 0) {
                            result = result + "ﺨ";
                        }
                        if (aus.compareTo("10010010") == 0) {
                            result = result + "ﺧ";
                        }
                        if (aus.compareTo("10010011") == 0) {
                            result = result + "ﺥ";
                        }
                        if (aus.compareTo("10010100") == 0) {
                            result = result + "ﺪ";
                        }
                        if (aus.compareTo("10010101") == 0) {
                            result = result + "ﺩ";
                        }
                        if (aus.compareTo("10010110") == 0) {
                            result = result + "ﺬ";
                        }
                        if (aus.compareTo("10010111") == 0) {
                            result = result + "ﺫ";
                        }
                        if (aus.compareTo("10011000") == 0) {
                            result = result + "ﺮ";
                        }
                        if (aus.compareTo("10011001") == 0) {
                            result = result + "ﺭ";
                        }
                        if (aus.compareTo("10011010") == 0) {
                            result = result + "ﺰ";
                        }
                        if (aus.compareTo("10011011") == 0) {
                            result = result + "ﺯ";
                        }
                        if (aus.compareTo("10011100") == 0) {
                            result = result + "ﺲ";
                        }
                        if (aus.compareTo("10011101") == 0) {
                            result = result + "ﺴ";
                        }
                        if (aus.compareTo("10011110") == 0) {
                            result = result + "ﺳ";
                        }
                        if (aus.compareTo("10011111") == 0) {
                            result = result + "ﺱ";
                        }
                        if (aus.compareTo("10100000") == 0) {
                            result = result + "ﺶ";
                        }
                        if (aus.compareTo("10100001") == 0) {
                            result = result + "ﺸ";
                        }
                        if (aus.compareTo("10100010") == 0) {
                            result = result + "ﺷ";
                        }
                        if (aus.compareTo("10100011") == 0) {
                            result = result + "ﺵ";
                        }
                        if (aus.compareTo("10100100") == 0) {
                            result = result + "ﺺ";
                        }
                        if (aus.compareTo("10100101") == 0) {
                            result = result + "ﺼ";
                        }
                        if (aus.compareTo("10100110") == 0) {
                            result = result + "ﺻ";
                        }
                        if (aus.compareTo("10100111") == 0) {
                            result = result + "ﺹ";
                        }
                        if (aus.compareTo("10101000") == 0) {
                            result = result + "ﺾ";
                        }
                        if (aus.compareTo("10101001") == 0) {
                            result = result + "ﻀ";
                        }
                        if (aus.compareTo("10101010") == 0) {
                            result = result + "ﺿ";
                        }
                        if (aus.compareTo("10101011") == 0) {
                            result = result + "ﺽ";
                        }
                        if (aus.compareTo("10101100") == 0) {
                            result = result + "ﻂ";
                        }
                        if (aus.compareTo("10101101") == 0) {
                            result = result + "ﻄ";
                        }
                        if (aus.compareTo("10101110") == 0) {
                            result = result + "ﻃ";
                        }
                        if (aus.compareTo("10101111") == 0) {
                            result = result + "ﻁ";
                        }
                        if (aus.compareTo("10110000") == 0) {
                            result = result + "ﻆ";
                        }
                        if (aus.compareTo("10110001") == 0) {
                            result = result + "ﻈ";
                        }
                        if (aus.compareTo("10110010") == 0) {
                            result = result + "ﻇ";
                        }
                        if (aus.compareTo("10110011") == 0) {
                            result = result + "ﻅ";
                        }
                        if (aus.compareTo("10110100") == 0) {
                            result = result + "ﻊ";
                        }
                        if (aus.compareTo("10110101") == 0) {
                            result = result + "ﻌ";
                        }
                        if (aus.compareTo("10110110") == 0) {
                            result = result + "ﻋ";
                        }
                        if (aus.compareTo("10110111") == 0) {
                            result = result + "ﻉ";
                        }
                        if (aus.compareTo("10111000") == 0) {
                            result = result + "ﻎ";
                        }
                        if (aus.compareTo("10111001") == 0) {
                            result = result + "ﻐ";
                        }
                        if (aus.compareTo("10111010") == 0) {
                            result = result + "ﻏ";
                        }
                        if (aus.compareTo("10111011") == 0) {
                            result = result + "ﻍ";
                        }
                        if (aus.compareTo("10111100") == 0) {
                            result = result + "ﻒ";
                        }
                        if (aus.compareTo("10111101") == 0) {
                            result = result + "ﻔ";
                        }
                        if (aus.compareTo("10111110") == 0) {
                            result = result + "ﻓ";
                        }
                        if (aus.compareTo("10111111") == 0) {
                            result = result + "ﻑ";
                        }
                        if (aus.compareTo("11000000") == 0) {
                            result = result + "ﻖ";
                        }
                        if (aus.compareTo("11000001") == 0) {
                            result = result + "ﻘ";
                        }
                        if (aus.compareTo("11000010") == 0) {
                            result = result + "ﻗ";
                        }
                        if (aus.compareTo("11000011") == 0) {
                            result = result + "ﻕ";
                        }
                        if (aus.compareTo("11000100") == 0) {
                            result = result + "ﻚ";
                        }
                        if (aus.compareTo("11000101") == 0) {
                            result = result + "ﻜ";
                        }
                        if (aus.compareTo("11000110") == 0) {
                            result = result + "ﻛ";
                        }
                        if (aus.compareTo("11000111") == 0) {
                            result = result + "ﻙ";
                        }
                        if (aus.compareTo("11001000") == 0) {
                            result = result + "ﻞ";
                        }
                        if (aus.compareTo("11001001") == 0) {
                            result = result + "ﻠ";
                        }
                        if (aus.compareTo("11001010") == 0) {
                            result = result + "ﻟ";
                        }
                        if (aus.compareTo("11001011") == 0) {
                            result = result + "ﻝ";
                        }
                        if (aus.compareTo("11001100") == 0) {
                            result = result + "ﻢ";
                        }
                        if (aus.compareTo("11001101") == 0) {
                            result = result + "ﻤ";
                        }
                        if (aus.compareTo("11001110") == 0) {
                            result = result + "ﻣ";
                        }
                        if (aus.compareTo("11001111") == 0) {
                            result = result + "ﻡ";
                        }
                        if (aus.compareTo("11010000") == 0) {
                            result = result + "ﻦ";
                        }
                        if (aus.compareTo("11010001") == 0) {
                            result = result + "ﻨ";
                        }
                        if (aus.compareTo("11010010") == 0) {
                            result = result + "ﻧ";
                        }
                        if (aus.compareTo("11010011") == 0) {
                            result = result + "ﻥ";
                        }
                        if (aus.compareTo("11010100") == 0) {
                            result = result + "ﻪ";
                        }
                        if (aus.compareTo("11010101") == 0) {
                            result = result + "ﻬ";
                        }
                        if (aus.compareTo("11010110") == 0) {
                            result = result + "ﻫ";
                        }
                        if (aus.compareTo("11010111") == 0) {
                            result = result + "ﻩ";
                        }
                        if (aus.compareTo("11011000") == 0) {
                            result = result + "ﻮ";
                        }
                        if (aus.compareTo("11011001") == 0) {
                            result = result + "ﻭ";
                        }
                        if (aus.compareTo("11011010") == 0) {
                            result = result + "ﻲ";
                        }
                        if (aus.compareTo("11011011") == 0) {
                            result = result + "ﻴ";
                        }
                        if (aus.compareTo("11011100") == 0) {
                            result = result + "ﻳ";
                        }
                        if (aus.compareTo("11011101") == 0) {
                            result = result + "ﻱ";
                        }
                        if (aus.compareTo("11011110") == 0) {
                            result = result + "ﺀ";
                        }
                        if (aus.compareTo("11011111") == 0) {
                            result = result + "ﺄ";
                        }
                        if (aus.compareTo("11100000") == 0) {
                            result = result + "ﺃ";
                        }
                        if (aus.compareTo("11100001") == 0) {
                            result = result + "ﺈ";
                        }
                        if (aus.compareTo("11100010") == 0) {
                            result = result + "ﺇ";
                        }
                        if (aus.compareTo("11100011") == 0) {
                            result = result + "ﺆ";
                        }
                        if (aus.compareTo("11100100") == 0) {
                            result = result + "ﺅ";
                        }
                        if (aus.compareTo("11100101") == 0) {
                            result = result + "ﺊ";
                        }
                        if (aus.compareTo("11100110") == 0) {
                            result = result + "ﺌ";
                        }
                        if (aus.compareTo("11100111") == 0) {
                            result = result + "ﺋ";
                        }
                        if (aus.compareTo("11101000") == 0) {
                            result = result + "ﺉ";
                        }
                        if (aus.compareTo("11101001") == 0) {
                            result = result + "ﺔ";
                        }
                        if (aus.compareTo("11101010") == 0) {
                            result = result + "ﺓ";
                        }
                        if (aus.compareTo("11101011") == 0) {
                            result = result + "ﻰ";
                        }
                        if (aus.compareTo("11101100") == 0) {
                            result = result + "ﻯ";
                        }
                        if (aus.compareTo("11101101") == 0) {
                            result = result + "ْ ";
                        }
                        if (aus.compareTo("11101110") == 0) {
                            result = result + "ُ ";
                        }
                        if (aus.compareTo("11101111") == 0) {
                            result = result + "ِ ";
                        }
                        if (aus.compareTo("11110000") == 0) {
                            result = result + "َ ";
                        }
                        if (aus.compareTo("11110001") == 0) {
                            result = result + "؟";
                        }
                        if (aus.compareTo("11110010") == 0) {
                            result = result + "٠";
                        }
                        if (aus.compareTo("11110011") == 0) {
                            result = result + "١";
                        }
                        if (aus.compareTo("11110100") == 0) {
                            result = result + "٢";
                        }
                        if (aus.compareTo("11110101") == 0) {
                            result = result + "٣";
                        }
                        if (aus.compareTo("11110110") == 0) {
                            result = result + "٤";
                        }
                        if (aus.compareTo("11110111") == 0) {
                            result = result + "٥";
                        }
                        if (aus.compareTo("11111000") == 0) {
                            result = result + "٦";
                        }
                        if (aus.compareTo("11111001") == 0) {
                            result = result + "٧";
                        }
                        if (aus.compareTo("11111010") == 0) {
                            result = result + "۸";
                        }
                        if (aus.compareTo("11111011") == 0) {
                            result = result + "٩";
                        }
                        if (aus.compareTo("11111100") == 0) {
                            result = result + "۶";
                        }
                        if (aus.compareTo("11111101") == 0) {
                            result = result + "۵";
                        }
                        if (aus.compareTo("11111110") == 0) {
                            result = result + "۴";
                        }
                        if (aus.compareTo("11111111") == 0) {
                            result = result + "،";
                        }
                        aus = "";
                    }
                }
            }
    }
}
