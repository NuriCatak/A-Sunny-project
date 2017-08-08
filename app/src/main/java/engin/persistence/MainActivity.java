package engin.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    //todo improve suggestions mechanism.
    //// TODO: 08.08.2017 when pressed on the enter  user should get the list of episodes whatched. 
    //// TODO: 08.08.2017  update the episode list

    //todo collect number of times an episode watched.

    String epName = "";
    String seenEpisodes;
    Button button1, button2, button3, sunny, foggy;
    TextView text1;
    EditText editText;
    int myRandom;
    Random randInt = new Random();
    ArrayList<Integer> list; ArrayList<String> epNameList;
    TinyDB  tinydb;
    RegexOutput regexOutput;
    ArrayList<String> regList;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mConditionRef = mRootRef.child("condition");
    DatabaseReference mEpisodesRef = mRootRef.child("Episodes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // gets the episode list
        mEpisodesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                ArrayList<String> epNameList = dataSnapshot.getValue(genericTypeIndicator );

                if(epNameList != null && !epNameList.isEmpty()) {
                    tinydb.putListString("episodeNames", epNameList);
                } else{
                    epNameList = new ArrayList<String>();
                    tinydb.putListString("episodeNames", epNameList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Context context = getApplicationContext();
        tinydb = new TinyDB(context);
        epNameList = tinydb.getListString("episodeNames");


        text1 = (TextView) findViewById(R.id.textView); // for displaying
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        sunny = (Button) findViewById(R.id.sunny);
        foggy = (Button) findViewById(R.id.foggy);
        editText = (EditText) findViewById(R.id.editText); // For input

//        // Shows the list of episodes
//        if((list != null) && !list.isEmpty() ) {
//            seenEpisodes = "Seen episodes are: ";
//            for (Integer in : list) {
//                seenEpisodes += String.valueOf(in) + " ";
//            }
//            text1.setText(seenEpisodes);
//        } else{
//            text1.setText("The list is empty");
//
//        }

        // Listen
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        sunny.setOnClickListener(this);
        foggy.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mConditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<ArrayList<Integer>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Integer>>() {};
                ArrayList<Integer> list = dataSnapshot.getValue(genericTypeIndicator );

                if(list != null && !list.isEmpty()) {
                    tinydb.putListInt("episodes", list);
                    seenEpisodes = "Seen episodes are: ";
                    for(Integer in: list){
                        seenEpisodes += String.valueOf(in) + " ";
                    }
                    text1.setText(seenEpisodes);
                } else{
                    text1.setText("The list is empty");
                    list = new ArrayList<Integer>();
                    tinydb.putListInt("episodes", list);

                }
//                Log.d("feuern", "onDataChange: "+ list.size() );
                //Collections.sort(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        list = tinydb.getListInt("episodes");

    sunny.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConditionRef.setValue("Sunny");
        }
    });
    foggy.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConditionRef.setValue("Foggy");
        }
    });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button: // Getting the input of watched episode number.
                String a  = editText.getText().toString();//editText.getText().toString();
//                regList.clear();


                try {
                    regList = new ArrayList<String>();
                    regexOutput = new RegexOutput(a, "\\d+");
                    regList = regexOutput.toArrayList();
                    Log.d("regex", regList.toString());


                    for (String s : regList) {
                        //if(s.equals(" "))continue; // Ignores multiple spaces.
                        int episodeNumber = Integer.parseInt(s);
                        if (list == null || list.size() == 0) {
                            list = new ArrayList<Integer>();
                            list.add(episodeNumber);
                            // Handle Exception

                        } else {
                            if (!list.contains(episodeNumber)) {
                                list.add(episodeNumber);
                                Collections.sort(list);
                            }
                        }

                    }
                } catch(IndexOutOfBoundsException Bounds){

                    String errorMessage = "Wrong input format.\nEnter  numbers. For multiple" +
                            " inputs, add spaces between them.";
                    text1.setText(errorMessage);
                    break;
                }
                tinydb.putListInt("episodes", list);
                Log.d("regex", String.valueOf(list.size()));
                mConditionRef.setValue(list);

                //then save currentString
                text1.setText("saved");
                break;

            case R.id.button2: // Showing the suggestion
                String cont;
                do{
                    myRandom = randInt.nextInt(10) +1;
                } while(list.contains(myRandom));
                //// TODO: 08.08.2017   get the episode name from the database
                epName = epNameList.size() <= myRandom -1 ? "not in the database" : epNameList.get(myRandom -1);


                cont = "\nRandom is: "+ String.valueOf(myRandom) + " " + epName;
                text1.setText(cont);
                //text1.setText(String.valueOf(myRandom));
                break;

            case R.id.button3: // For deleting the list.
                list.clear();
                tinydb.putListInt("episodes", list);
                mConditionRef.setValue(list);
                text1.setText("cleared the list");

                break;
        }
    }
    public class RegexOutput{
        ArrayList list1;
        String textInput, regexInput;
        Pattern pattern;
        Matcher match;
        public RegexOutput(String textInput, String regexInput) {
            this.textInput = textInput;
            this.regexInput = regexInput;
        }

        public ArrayList toArrayList(){
            pattern = Pattern.compile(regexInput);
            match = pattern.matcher(textInput);
            list1 = new ArrayList<String>();
            while(match.find()){
                if(!match.group().isEmpty()){
                    list1.add(match.group().toString());
                }
            }


            return list1;
        }
    }
}
