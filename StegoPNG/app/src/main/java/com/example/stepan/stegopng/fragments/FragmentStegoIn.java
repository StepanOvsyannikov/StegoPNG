package com.example.stepan.stegopng.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stepan.stegopng.MersenneTwister;
import com.example.stepan.stegopng.R;
import com.example.stepan.stegopng.Save2;

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


public class FragmentStegoIn extends Fragment implements View.OnClickListener {
    View rootView;
    CircleButton Circlebutton_stego_in;

    Bitmap Stego_PNG = null;

    TextView textView2;
    TextView textView3;

    String test_str = "";
    String test2_str = "";

    int[] numbers;
    int[] massiv_for_all_binary;

    String BinaryMESSAGE = "file_for_BinaryMESSAGE";
    String OpenKEY = "file_for_OpenKEY";
    String Message;
    String str_12_bit;
    String all_binary;
    String open_key_to_hash;

    MersenneTwister rand_gamma;

    Set<Integer> arraySet_gamma;

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
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_stego_in, container, false);

        textView2 = rootView.findViewById(R.id.textView2);
        textView3 = rootView.findViewById(R.id.textView3);

        Circlebutton_stego_in = rootView.findViewById(R.id.Circlebutton_stego_in);
        Circlebutton_stego_in.setOnClickListener(this);

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
                    Circlebutton_stego_in.setClickable(false);
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
            case R.id.Circlebutton_stego_in:
                read_Binary_Message();
                Message_to_binary_with_12_bit();
                read_open_key();

                Generate_numbers_for_pixels();



                //selectedImage = RotateBitmap(selectedImage, 180);

                try {
                    Stego_PNG = processingBitmap(selectedImage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Save2 savefile = new Save2();

                savefile.SaveImage(getActivity(), Stego_PNG);

                for (int i = 0; i < numbers.length; i++)
                {
                    test2_str += " " + Integer.toString(numbers[i]);
                }

                showToast("Сообщение записано");

                //textView2.setText(test_str);
                //textView3.setText(test2_str);

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

    void read_Binary_Message() {
        Message = "";
        try {
            BufferedReader br_for_open_text = new BufferedReader(new InputStreamReader(getActivity().openFileInput(BinaryMESSAGE)));
            String str_for_open_text = "";
            while ((str_for_open_text = br_for_open_text.readLine()) != null) {
                Message = Message + str_for_open_text;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void Message_to_binary_with_12_bit() {
        str_12_bit = Integer.toBinaryString(Message.length());
        while (str_12_bit.length() < 12) {
            str_12_bit = "0" + str_12_bit;
        }
        all_binary = str_12_bit + Message; // вся строка под встраивание

        massiv_for_all_binary = new int[all_binary.length()];

        for (int i = 0; i < all_binary.length(); i++)
        {
            char c = all_binary.charAt(i);
            String s = String.valueOf(c);
            massiv_for_all_binary[i] = Integer.parseInt(s);
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
    void Generate_numbers_for_pixels()
    {
        res = "";
        numbers = new int[all_binary.length()];
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
        } while(j < all_binary.length());
    }


    private Bitmap processingBitmap(Bitmap src) throws InterruptedException {
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        //int j = 0;
        int width = src.getWidth();

        for (int x = 0; x < src.getWidth(); x++)
        {
            for (int y = 0; y < src.getHeight(); y++)
            {
                int pixelColor = src.getPixel(x, y);
                // получим информацию о прозрачности
                int pixelAlpha = Color.alpha(pixelColor);
                // получим цвет пикселя для корректировки
                int pixelRed = Color.red(pixelColor);
                int pixelGreen = Color.green(pixelColor);
                int pixelBlue = Color.blue(pixelColor);

                int newPixel= Color.argb(
                        pixelAlpha, pixelRed, pixelGreen, pixelBlue);
                dest.setPixel(x, y, newPixel);
            }
        }


        int count = 0;
        for (int i = 0; i < numbers.length; i++)
        {
            int x = numbers[i] % src.getWidth();
            int y = numbers[i] / src.getWidth();
            // получим пиксель для корректировки
            int pixelColor = src.getPixel(x, y);
            // получим информацию о прозрачности
            int pixelAlpha = Color.alpha(pixelColor);
            // получим цвет пикселя для корректировки
            int pixelRed = Color.red(pixelColor);
            int pixelGreen = Color.green(pixelColor);
            int pixelBlue = Color.blue(pixelColor);

            String str_bin_blue = Integer.toBinaryString(pixelBlue);
            while (str_bin_blue.length() < 8)
            {
                str_bin_blue = "0" + str_bin_blue;
            } // длина точно 8;

            //massiv_for_all_binary[0] = 1;
            test_str += " " + Integer.toString(massiv_for_all_binary[i]);
            str_bin_blue = str_bin_blue.substring(0, str_bin_blue.length() - 1) + Integer.toString(massiv_for_all_binary[i]);


            int new_blue = Integer.parseInt(str_bin_blue, 2);

            int newPixel = Color.argb(
                    pixelAlpha, pixelRed, pixelGreen, new_blue);
            dest.setPixel(x, y, newPixel);

        }

        return  dest;
    }



    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
