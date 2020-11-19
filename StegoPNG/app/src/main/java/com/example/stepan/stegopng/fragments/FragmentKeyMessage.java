package com.example.stepan.stegopng.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stepan.stegopng.R;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import at.markushi.ui.CircleButton;

import static android.content.Context.MODE_PRIVATE;

public class FragmentKeyMessage extends Fragment implements View.OnClickListener {
    View rootView;

    String HashKEY = "file_for_HashKEY";
    String OpenKEY = "file_for_OpenKEY";
    String BinaryMESSAGE = "file_for_BinaryMESSAGE";

    CircleButton CircleButton_key;
    CircleButton CircleButton_message;

    EditText editText_for_key;
    EditText editText_for_message;

    TextView textView_for_key;
    TextView textView_for_message;

    String string_for_openText;
    String string_for_hash;
    String secret_message;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_key_message, container, false);

        textView_for_key = rootView.findViewById(R.id.textView_for_key);
        textView_for_message = rootView.findViewById(R.id.textView_for_message);

        editText_for_key = rootView.findViewById(R.id.editText_for_key);
        editText_for_message = rootView.findViewById(R.id.editText_for_message);

        CircleButton_key = rootView.findViewById(R.id.Circlebutton_key);
        CircleButton_key.setOnClickListener(this);
        CircleButton_message = rootView.findViewById(R.id.Circlebutton_message);
        CircleButton_message.setOnClickListener(this);

        text_for_key();
        text_for_message();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Circlebutton_key:
                string_for_openText = editText_for_key.getText().toString();
                string_for_hash = hashPassword(string_for_openText, 512);
                write_open_key();
                write_hash_key();
                showToast("Хэш от стегоключа записан");
                break;
            case R.id.Circlebutton_message:
                secret_message = editText_for_message.getText().toString();
                write_binary_message();
                showToast("Сообщение в двоичном виде записано");
                break;
            default:
                break;
        }
    }

    public void showToast(String string)
    {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    public void text_for_message()
    {
        editText_for_message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (editText_for_message.getText().length() == 0) {
                    CircleButton_message.setClickable(false);
                    editText_for_message.setError("Введите сообщение для скрытой передачи");

                }
            }
        });

        editText_for_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int change = 500 - editText_for_message.getText().length();
                textView_for_message.setText("Осталось: " + Integer.toString(change));
                CircleButton_message.setClickable(true);
                if (change < 0)
                {
                    CircleButton_message.setClickable(false);
                    textView_for_message.setText("Слишком большое сообщение");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    public void text_for_key()
    {
        editText_for_key.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (editText_for_key.getText().length() == 0) {
                    CircleButton_key.setClickable(false);
                    editText_for_key.setError("Введите стегоключ");

                }
            }
        });

        editText_for_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int change = 10 - editText_for_key.getText().length();
                textView_for_key.setText("Осталось: " + Integer.toString(change));
                CircleButton_key.setClickable(false);
                if (change <= 0)
                {
                    CircleButton_key.setClickable(true);
                    textView_for_key.setText("Длина ключа приемлима");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    public static String hashPassword(String password, int len) {

        SHA3.DigestSHA3 md = new SHA3.DigestSHA3(len);
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return Hex.toHexString(digest);
    }

    void write_hash_key() {
        try {
            BufferedWriter bw4 = new BufferedWriter(new OutputStreamWriter(getActivity().openFileOutput(HashKEY, MODE_PRIVATE)));
            bw4.write(string_for_hash);
            bw4.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write_open_key() {
        try {
            BufferedWriter bw4 = new BufferedWriter(new OutputStreamWriter(getActivity().openFileOutput(OpenKEY, MODE_PRIVATE)));
            bw4.write(string_for_openText);
            bw4.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    void write_binary_message() {
        try {
            String str_in = "";
            String s = "";
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getActivity().openFileOutput(BinaryMESSAGE, MODE_PRIVATE)));

            String arrStr[] = secret_message.split("");

            for (int i = 0; i < secret_message.length() + 1; i++) {
                if (i != 0) {

                    s = "";
                    // Нижний регистр
                    if (arrStr[i].compareTo("а") == 0) {
                        s = "00000000";
                    }
                    if (arrStr[i].compareTo("б") == 0) {
                        s = "00000001";
                    }
                    if (arrStr[i].compareTo("в") == 0) {
                        s = "00000010";
                    }
                    if (arrStr[i].compareTo("г") == 0) {
                        s = "00000011";
                    }
                    if (arrStr[i].compareTo("д") == 0) {
                        s = "00000100";
                    }
                    if (arrStr[i].compareTo("е") == 0) {
                        s = "00000101";
                    }
                    if (arrStr[i].compareTo("ё") == 0) {
                        s = "00000110";
                    }
                    if (arrStr[i].compareTo("ж") == 0) {
                        s = "00000111";
                    }
                    if (arrStr[i].compareTo("з") == 0) {
                        s = "00001000";
                    }
                    if (arrStr[i].compareTo("и") == 0) {
                        s = "00001001";
                    }
                    if (arrStr[i].compareTo("й") == 0) {
                        s = "00001010";
                    }
                    if (arrStr[i].compareTo("к") == 0) {
                        s = "00001011";
                    }
                    if (arrStr[i].compareTo("л") == 0) {
                        s = "00001100";
                    }
                    if (arrStr[i].compareTo("м") == 0) {
                        s = "00001101";
                    }
                    if (arrStr[i].compareTo("н") == 0) {
                        s = "00001110";
                    }
                    if (arrStr[i].compareTo("о") == 0) {
                        s = "00001111";
                    }
                    if (arrStr[i].compareTo("п") == 0) {
                        s = "00010000";
                    }
                    if (arrStr[i].compareTo("р") == 0) {
                        s = "00010001";
                    }
                    if (arrStr[i].compareTo("с") == 0) {
                        s = "00010010";
                    }
                    if (arrStr[i].compareTo("т") == 0) {
                        s = "00010011";
                    }
                    if (arrStr[i].compareTo("у") == 0) {
                        s = "00010100";
                    }
                    if (arrStr[i].compareTo("ф") == 0) {
                        s = "00010101";
                    }
                    if (arrStr[i].compareTo("х") == 0) {
                        s = "00010110";
                    }
                    if (arrStr[i].compareTo("ц") == 0) {
                        s = "00010111";
                    }
                    if (arrStr[i].compareTo("ч") == 0) {
                        s = "00011000";
                    }
                    if (arrStr[i].compareTo("ш") == 0) {
                        s = "00011001";
                    }
                    if (arrStr[i].compareTo("щ") == 0) {
                        s = "00011010";
                    }
                    if (arrStr[i].compareTo("ъ") == 0) {
                        s = "00011011";
                    }
                    if (arrStr[i].compareTo("ы") == 0) {
                        s = "00011100";
                    }
                    if (arrStr[i].compareTo("ь") == 0) {
                        s = "00011101";
                    }
                    if (arrStr[i].compareTo("э") == 0) {
                        s = "00011110";
                    }
                    if (arrStr[i].compareTo("ю") == 0) {
                        s = "00011111";
                    }
                    if (arrStr[i].compareTo("я") == 0) {
                        s = "00100000";
                    }
                    if (arrStr[i].compareTo("А") == 0) {
                        s = "00100001";
                    }
                    if (arrStr[i].compareTo("Б") == 0) {
                        s = "00100010";
                    }
                    if (arrStr[i].compareTo("В") == 0) {
                        s = "00100011";
                    }
                    if (arrStr[i].compareTo("Г") == 0) {
                        s = "00100100";
                    }
                    if (arrStr[i].compareTo("Д") == 0) {
                        s = "00100101";
                    }
                    if (arrStr[i].compareTo("Е") == 0) {
                        s = "00100110";
                    }
                    if (arrStr[i].compareTo("Ё") == 0) {
                        s = "00100111";
                    }
                    if (arrStr[i].compareTo("Ж") == 0) {
                        s = "00101000";
                    }
                    if (arrStr[i].compareTo("З") == 0) {
                        s = "00101001";
                    }
                    if (arrStr[i].compareTo("И") == 0) {
                        s = "00101010";
                    }
                    if (arrStr[i].compareTo("Й") == 0) {
                        s = "00101011";
                    }
                    if (arrStr[i].compareTo("К") == 0) {
                        s = "00101100";
                    }
                    if (arrStr[i].compareTo("Л") == 0) {
                        s = "00101101";
                    }
                    if (arrStr[i].compareTo("М") == 0) {
                        s = "00101110";
                    }
                    if (arrStr[i].compareTo("Н") == 0) {
                        s = "00101111";
                    }
                    if (arrStr[i].compareTo("О") == 0) {
                        s = "00110000";
                    }
                    if (arrStr[i].compareTo("П") == 0) {
                        s = "00110001";
                    }
                    if (arrStr[i].compareTo("Р") == 0) {
                        s = "00110010";
                    }
                    if (arrStr[i].compareTo("С") == 0) {
                        s = "00110011";
                    }
                    if (arrStr[i].compareTo("Т") == 0) {
                        s = "00110100";
                    }
                    if (arrStr[i].compareTo("У") == 0) {
                        s = "00110101";
                    }
                    if (arrStr[i].compareTo("Ф") == 0) {
                        s = "00110110";
                    }
                    if (arrStr[i].compareTo("Х") == 0) {
                        s = "00110111";
                    }
                    if (arrStr[i].compareTo("Ц") == 0) {
                        s = "00111000";
                    }
                    if (arrStr[i].compareTo("Ч") == 0) {
                        s = "00111001";
                    }
                    if (arrStr[i].compareTo("Ш") == 0) {
                        s = "00111010";
                    }
                    if (arrStr[i].compareTo("Щ") == 0) {
                        s = "00111011";
                    }
                    if (arrStr[i].compareTo("Ъ") == 0) {
                        s = "00111100";
                    }
                    if (arrStr[i].compareTo("Ы") == 0) {
                        s = "00111101";
                    }
                    if (arrStr[i].compareTo("Ь") == 0) {
                        s = "00111110";
                    }
                    if (arrStr[i].compareTo("Э") == 0) {
                        s = "00111111";
                    }
                    if (arrStr[i].compareTo("Ю") == 0) {
                        s = "01000000";
                    }
                    if (arrStr[i].compareTo("Я") == 0) {
                        s = "01000001";
                    }
                    if (arrStr[i].compareTo(" ") == 0) {
                        s = "01000010";
                    }
                    if (arrStr[i].compareTo("=") == 0) {
                        s = "01000011";
                    }
                    if (arrStr[i].compareTo(",") == 0) {
                        s = "01000100";
                    }
                    if (arrStr[i].compareTo(".") == 0) {
                        s = "01000101";
                    }
                    if (arrStr[i].compareTo("!") == 0) {
                        s = "01000110";
                    }
                    if (arrStr[i].compareTo("?") == 0) {
                        s = "01000111";
                    }
                    if (arrStr[i].compareTo(":") == 0) {
                        s = "01001000";
                    }
                    if (arrStr[i].compareTo("'") == 0) {
                        s = "01001001";
                    }
                    if (arrStr[i].compareTo("-") == 0) {
                        s = "01001010";
                    }
                    if (arrStr[i].compareTo("0") == 0) {
                        s = "01001011";
                    }
                    if (arrStr[i].compareTo("1") == 0) {
                        s = "01001100";
                    }
                    if (arrStr[i].compareTo("2") == 0) {
                        s = "01001101";
                    }
                    if (arrStr[i].compareTo("3") == 0) {
                        s = "01001110";
                    }
                    if (arrStr[i].compareTo("4") == 0) {
                        s = "01001111";
                    }
                    if (arrStr[i].compareTo("5") == 0) {
                        s = "01010000";
                    }
                    if (arrStr[i].compareTo("6") == 0) {
                        s = "01010001";
                    }
                    if (arrStr[i].compareTo("7") == 0) {
                        s = "01010010";
                    }
                    if (arrStr[i].compareTo("8") == 0) {
                        s = "01010011";
                    }
                    if (arrStr[i].compareTo("9") == 0) {
                        s = "01010100";
                    }
                    if (arrStr[i].compareTo(")") == 0) {
                        s = "01010101";
                    }
                    if (arrStr[i].compareTo("(") == 0) {
                        s = "01010110";
                    }
                    if (arrStr[i].compareTo("*") == 0) {
                        s = "01010111";
                    }
                    if (arrStr[i].compareTo(";") == 0) {
                        s = "01011000";
                    }
                    if (arrStr[i].compareTo("&") == 0) {
                        s = "01011001";
                    }
                    if (arrStr[i].compareTo("@") == 0) {
                        s = "01011010";
                    }
                    if (arrStr[i].compareTo("%") == 0) {
                        s = "01011011";
                    }
                    if (arrStr[i].compareTo("$") == 0) {
                        s = "01011100";
                    }
                    if (arrStr[i].compareTo("+") == 0) {
                        s = "01011101";
                    }
                    if (arrStr[i].compareTo("#") == 0) {
                        s = "01011110";
                    }
                    if (arrStr[i].compareTo("\"") == 0) {
                        s = "01011111";
                    }
                    if (arrStr[i].compareTo("a") == 0) {
                        s = "01100000";
                    }
                    if (arrStr[i].compareTo("b") == 0) {
                        s = "01100001";
                    }
                    if (arrStr[i].compareTo("c") == 0) {
                        s = "01100010";
                    }
                    if (arrStr[i].compareTo("d") == 0) {
                        s = "01100011";
                    }
                    if (arrStr[i].compareTo("e") == 0) {
                        s = "01100100";
                    }
                    if (arrStr[i].compareTo("f") == 0) {
                        s = "01100101";
                    }
                    if (arrStr[i].compareTo("g") == 0) {
                        s = "01100110";
                    }
                    if (arrStr[i].compareTo("h") == 0) {
                        s = "01100111";
                    }
                    if (arrStr[i].compareTo("i") == 0) {
                        s = "01101000";
                    }
                    if (arrStr[i].compareTo("j") == 0) {
                        s = "01101001";
                    }
                    if (arrStr[i].compareTo("k") == 0) {
                        s = "01101010";
                    }
                    if (arrStr[i].compareTo("l") == 0) {
                        s = "01101011";
                    }
                    if (arrStr[i].compareTo("m") == 0) {
                        s = "01101100";
                    }
                    if (arrStr[i].compareTo("n") == 0) {
                        s = "01101101";
                    }
                    if (arrStr[i].compareTo("o") == 0) {
                        s = "01101110";
                    }
                    if (arrStr[i].compareTo("p") == 0) {
                        s = "01101111";
                    }
                    if (arrStr[i].compareTo("q") == 0) {
                        s = "01110000";
                    }
                    if (arrStr[i].compareTo("r") == 0) {
                        s = "01110001";
                    }
                    if (arrStr[i].compareTo("s") == 0) {
                        s = "01110010";
                    }
                    if (arrStr[i].compareTo("t") == 0) {
                        s = "01110011";
                    }
                    if (arrStr[i].compareTo("u") == 0) {
                        s = "01110100";
                    }
                    if (arrStr[i].compareTo("v") == 0) {
                        s = "01110101";
                    }
                    if (arrStr[i].compareTo("w") == 0) {
                        s = "01110110";
                    }
                    if (arrStr[i].compareTo("x") == 0) {
                        s = "01110111";
                    }
                    if (arrStr[i].compareTo("y") == 0) {
                        s = "01111000";
                    }
                    if (arrStr[i].compareTo("z") == 0) {
                        s = "01111001";
                    }
                    if (arrStr[i].compareTo("ﺎ") == 0) {
                        s = "01111010";
                    }
                    if (arrStr[i].compareTo("ﺍ") == 0) {
                        s = "01111011";
                    }
                    if (arrStr[i].compareTo("ﺐ") == 0) {
                        s = "01111100";
                    }
                    if (arrStr[i].compareTo("ﺒ") == 0) {
                        s = "01111101";
                    }
                    if (arrStr[i].compareTo("ﺑ") == 0) {
                        s = "01111110";
                    }
                    if (arrStr[i].compareTo("ﺏ") == 0) {
                        s = "01111111";
                    }
                    if (arrStr[i].compareTo("ﺖ") == 0) {
                        s = "10000000";
                    }
                    if (arrStr[i].compareTo("ﺘ") == 0) {
                        s = "10000001";
                    }
                    if (arrStr[i].compareTo("ﺗ") == 0) {
                        s = "10000010";
                    }
                    if (arrStr[i].compareTo("ﺕ") == 0) {
                        s = "10000011";
                    }
                    if (arrStr[i].compareTo("ﺚ") == 0) {
                        s = "10000100";
                    }
                    if (arrStr[i].compareTo("ﺜ") == 0) {
                        s = "10000101";
                    }
                    if (arrStr[i].compareTo("ﺛ") == 0) {
                        s = "10000110";
                    }
                    if (arrStr[i].compareTo("ﺙ") == 0) {
                        s = "10000111";
                    }
                    if (arrStr[i].compareTo("ﺞ") == 0) {
                        s = "10001000";
                    }
                    if (arrStr[i].compareTo("ﺠ") == 0) {
                        s = "10001001";
                    }
                    if (arrStr[i].compareTo("ﺟ") == 0) {
                        s = "10001010";
                    }
                    if (arrStr[i].compareTo("ﺝ") == 0) {
                        s = "10001011";
                    }
                    if (arrStr[i].compareTo("ﺢ") == 0) {
                        s = "10001100";
                    }
                    if (arrStr[i].compareTo("ﺤ") == 0) {
                        s = "10001101";
                    }
                    if (arrStr[i].compareTo("ﺣ") == 0) {
                        s = "10001110";
                    }
                    if (arrStr[i].compareTo("ﺡ") == 0) {
                        s = "10001111";
                    }
                    if (arrStr[i].compareTo("ﺦ") == 0) {
                        s = "10010000";
                    }
                    if (arrStr[i].compareTo("ﺨ") == 0) {
                        s = "10010001";
                    }
                    if (arrStr[i].compareTo("ﺧ") == 0) {
                        s = "10010010";
                    }
                    if (arrStr[i].compareTo("ﺥ") == 0) {
                        s = "10010011";
                    }
                    if (arrStr[i].compareTo("ﺪ") == 0) {
                        s = "10010100";
                    }
                    if (arrStr[i].compareTo("ﺩ") == 0) {
                        s = "10010101";
                    }
                    if (arrStr[i].compareTo("ﺬ") == 0) {
                        s = "10010110";
                    }
                    if (arrStr[i].compareTo("ﺫ") == 0) {
                        s = "10010111";
                    }
                    if (arrStr[i].compareTo("ﺮ") == 0) {
                        s = "10011000";
                    }
                    if (arrStr[i].compareTo("ﺭ") == 0) {
                        s = "10011001";
                    }
                    if (arrStr[i].compareTo("ﺰ") == 0) {
                        s = "10011010";
                    }
                    if (arrStr[i].compareTo("ﺯ") == 0) {
                        s = "10011011";
                    }
                    if (arrStr[i].compareTo("ﺲ") == 0) {
                        s = "10011100";
                    }
                    if (arrStr[i].compareTo("ﺴ") == 0) {
                        s = "10011101";
                    }
                    if (arrStr[i].compareTo("ﺳ") == 0) {
                        s = "10011110";
                    }
                    if (arrStr[i].compareTo("ﺱ") == 0) {
                        s = "10011111";
                    }
                    if (arrStr[i].compareTo("ﺶ") == 0) {
                        s = "10100000";
                    }
                    if (arrStr[i].compareTo("ﺸ") == 0) {
                        s = "10100001";
                    }
                    if (arrStr[i].compareTo("ﺷ") == 0) {
                        s = "10100010";
                    }
                    if (arrStr[i].compareTo("ﺵ") == 0) {
                        s = "10100011";
                    }
                    if (arrStr[i].compareTo("ﺺ") == 0) {
                        s = "10100100";
                    }
                    if (arrStr[i].compareTo("ﺼ") == 0) {
                        s = "10100101";
                    }
                    if (arrStr[i].compareTo("ﺻ") == 0) {
                        s = "10100110";
                    }
                    if (arrStr[i].compareTo("ﺹ") == 0) {
                        s = "10100111";
                    }
                    if (arrStr[i].compareTo("ﺾ") == 0) {
                        s = "10101000";
                    }
                    if (arrStr[i].compareTo("ﻀ") == 0) {
                        s = "10101001";
                    }
                    if (arrStr[i].compareTo("ﺿ") == 0) {
                        s = "10101010";
                    }
                    if (arrStr[i].compareTo("ﺽ") == 0) {
                        s = "10101011";
                    }
                    if (arrStr[i].compareTo("ﻂ") == 0) {
                        s = "10101100";
                    }
                    if (arrStr[i].compareTo("ﻄ") == 0) {
                        s = "10101101";
                    }
                    if (arrStr[i].compareTo("ﻃ") == 0) {
                        s = "10101110";
                    }
                    if (arrStr[i].compareTo("ﻁ") == 0) {
                        s = "10101111";
                    }
                    if (arrStr[i].compareTo("ﻆ") == 0) {
                        s = "10110000";
                    }
                    if (arrStr[i].compareTo("ﻈ") == 0) {
                        s = "10110001";
                    }
                    if (arrStr[i].compareTo("ﻇ") == 0) {
                        s = "10110010";
                    }
                    if (arrStr[i].compareTo("ﻅ") == 0) {
                        s = "10110011";
                    }
                    if (arrStr[i].compareTo("ﻊ") == 0) {
                        s = "10110100";
                    }
                    if (arrStr[i].compareTo("ﻌ") == 0) {
                        s = "10110101";
                    }
                    if (arrStr[i].compareTo("ﻋ") == 0) {
                        s = "10110110";
                    }
                    if (arrStr[i].compareTo("ﻉ") == 0) {
                        s = "10110111";
                    }
                    if (arrStr[i].compareTo("ﻎ") == 0) {
                        s = "10111000";
                    }
                    if (arrStr[i].compareTo("ﻐ") == 0) {
                        s = "10111001";
                    }
                    if (arrStr[i].compareTo("ﻏ") == 0) {
                        s = "10111010";
                    }
                    if (arrStr[i].compareTo("ﻍ") == 0) {
                        s = "10111011";
                    }
                    if (arrStr[i].compareTo("ﻒ") == 0) {
                        s = "10111100";
                    }
                    if (arrStr[i].compareTo("ﻔ") == 0) {
                        s = "10111101";
                    }
                    if (arrStr[i].compareTo("ﻓ") == 0) {
                        s = "10111110";
                    }
                    if (arrStr[i].compareTo("ﻑ") == 0) {
                        s = "10111111";
                    }
                    if (arrStr[i].compareTo("ﻖ") == 0) {
                        s = "11000000";
                    }
                    if (arrStr[i].compareTo("ﻘ") == 0) {
                        s = "11000001";
                    }
                    if (arrStr[i].compareTo("ﻗ") == 0) {
                        s = "11000010";
                    }
                    if (arrStr[i].compareTo("ﻕ") == 0) {
                        s = "11000011";
                    }
                    if (arrStr[i].compareTo("ﻚ") == 0) {
                        s = "11000100";
                    }
                    if (arrStr[i].compareTo("ﻜ") == 0) {
                        s = "11000101";
                    }
                    if (arrStr[i].compareTo("ﻛ") == 0) {
                        s = "11000110";
                    }
                    if (arrStr[i].compareTo("ﻙ") == 0) {
                        s = "11000111";
                    }
                    if (arrStr[i].compareTo("ﻞ") == 0) {
                        s = "11001000";
                    }
                    if (arrStr[i].compareTo("ﻠ") == 0) {
                        s = "11001001";
                    }
                    if (arrStr[i].compareTo("ﻟ") == 0) {
                        s = "11001010";
                    }
                    if (arrStr[i].compareTo("ﻝ") == 0) {
                        s = "11001011";
                    }
                    if (arrStr[i].compareTo("ﻢ") == 0) {
                        s = "11001100";
                    }
                    if (arrStr[i].compareTo("ﻤ") == 0) {
                        s = "11001101";
                    }
                    if (arrStr[i].compareTo("ﻣ") == 0) {
                        s = "11001110";
                    }
                    if (arrStr[i].compareTo("ﻡ") == 0) {
                        s = "11001111";
                    }
                    if (arrStr[i].compareTo("ﻦ") == 0) {
                        s = "11010000";
                    }
                    if (arrStr[i].compareTo("ﻨ") == 0) {
                        s = "11010001";
                    }
                    if (arrStr[i].compareTo("ﻧ") == 0) {
                        s = "11010010";
                    }
                    if (arrStr[i].compareTo("ﻥ") == 0) {
                        s = "11010011";
                    }
                    if (arrStr[i].compareTo("ﻪ") == 0) {
                        s = "11010100";
                    }
                    if (arrStr[i].compareTo("ﻬ") == 0) {
                        s = "11010101";
                    }
                    if (arrStr[i].compareTo("ﻫ") == 0) {
                        s = "11010110";
                    }
                    if (arrStr[i].compareTo("ﻩ") == 0) {
                        s = "11010111";
                    }
                    if (arrStr[i].compareTo("ﻮ") == 0) {
                        s = "11011000";
                    }
                    if (arrStr[i].compareTo("ﻭ") == 0) {
                        s = "11011001";
                    }
                    if (arrStr[i].compareTo("ﻲ") == 0) {
                        s = "11011010";
                    }
                    if (arrStr[i].compareTo("ﻴ") == 0) {
                        s = "11011011";
                    }
                    if (arrStr[i].compareTo("ﻳ") == 0) {
                        s = "11011100";
                    }
                    if (arrStr[i].compareTo("ﻱ") == 0) {
                        s = "11011101";
                    }
                    if (arrStr[i].compareTo("ﺀ") == 0) {
                        s = "11011110";
                    }
                    if (arrStr[i].compareTo("ﺄ") == 0) {
                        s = "11011111";
                    }
                    if (arrStr[i].compareTo("ﺃ") == 0) {
                        s = "11100000";
                    }
                    if (arrStr[i].compareTo("ﺈ") == 0) {
                        s = "11100001";
                    }
                    if (arrStr[i].compareTo("ﺇ") == 0) {
                        s = "11100010";
                    }
                    if (arrStr[i].compareTo("ﺆ") == 0) {
                        s = "11100011";
                    }
                    if (arrStr[i].compareTo("ﺅ") == 0) {
                        s = "11100100";
                    }
                    if (arrStr[i].compareTo("ﺊ") == 0) {
                        s = "11100101";
                    }
                    if (arrStr[i].compareTo("ﺌ") == 0) {
                        s = "11100110";
                    }
                    if (arrStr[i].compareTo("ﺋ") == 0) {
                        s = "11100111";
                    }
                    if (arrStr[i].compareTo("ﺉ") == 0) {
                        s = "11101000";
                    }
                    if (arrStr[i].compareTo("ﺔ") == 0) {
                        s = "11101001";
                    }
                    if (arrStr[i].compareTo("ﺓ") == 0) {
                        s = "11101010";
                    }
                    if (arrStr[i].compareTo("ﻰ") == 0) {
                        s = "11101011";
                    }
                    if (arrStr[i].compareTo("ﻯ") == 0) {
                        s = "11101100";
                    }
                    if (arrStr[i].compareTo("ْ ") == 0) {
                        s = "11101101";
                    }
                    if (arrStr[i].compareTo("ُ ") == 0) {
                        s = "11101110";
                    }
                    if (arrStr[i].compareTo("ِ ") == 0) {
                        s = "11101111";
                    }
                    if (arrStr[i].compareTo("َ ") == 0) {
                        s = "11110000";
                    }
                    if (arrStr[i].compareTo("؟") == 0) {
                        s = "11110001";
                    }
                    if (arrStr[i].compareTo("٠") == 0) {
                        s = "11110010";
                    }
                    if (arrStr[i].compareTo("١") == 0) {
                        s = "11110011";
                    }
                    if (arrStr[i].compareTo("٢") == 0) {
                        s = "11110100";
                    }
                    if (arrStr[i].compareTo("٣") == 0) {
                        s = "11110101";
                    }
                    if (arrStr[i].compareTo("٤") == 0) {
                        s = "11110110";
                    }
                    if (arrStr[i].compareTo("٥") == 0) {
                        s = "11110111";
                    }
                    if (arrStr[i].compareTo("٦") == 0) {
                        s = "11111000";
                    }
                    if (arrStr[i].compareTo("٧") == 0) {
                        s = "11111001";
                    }
                    if (arrStr[i].compareTo("۸") == 0) {
                        s = "11111010";
                    }
                    if (arrStr[i].compareTo("٩") == 0) {
                        s = "11111011";
                    }
                    if (arrStr[i].compareTo("۶") == 0) {
                        s = "11111100";
                    }
                    if (arrStr[i].compareTo("۵") == 0) {
                        s = "11111101";
                    }
                    if (arrStr[i].compareTo("۴") == 0) {
                        s = "11111110";
                    }
                    if (arrStr[i].compareTo("،") == 0) {
                        s = "11111111";
                    }
                    str_in += s;
                }

            }
            bw.write(str_in);
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
