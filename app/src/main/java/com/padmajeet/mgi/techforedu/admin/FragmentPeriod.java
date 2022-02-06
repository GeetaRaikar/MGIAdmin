package com.padmajeet.mgi.techforedu.admin;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.admin.model.Batch;
import com.padmajeet.mgi.techforedu.admin.model.FeedbackCategory;
import com.padmajeet.mgi.techforedu.admin.model.Period;
import com.padmajeet.mgi.techforedu.admin.model.Staff;
import com.padmajeet.mgi.techforedu.admin.util.SessionManager;
import com.padmajeet.mgi.techforedu.admin.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPeriod extends Fragment {

    private View view = null;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference periodCollectionRef=db.collection("Period");
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private CollectionReference timeTableCollectionRef=db.collection("TimeTable");
    private Period period;
    private List<Period> periodList=new ArrayList<>();
    private Batch batch;
    private List<Batch> batchList=new ArrayList<>();
    private Spinner spBatch;
    private RecyclerView rvPeriod;
    private RecyclerView.Adapter periodAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Gson gson;
    private Staff loggedInUser;
    private String instituteId,academicYearId;
    private Batch selectedBatch;
    private Bundle bundle = new Bundle();
    private Batch currentBatch;
    private LinearLayout llNoList;
    private String selectedBatchPeriod;
    private Fragment currentFragment;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private EditText etToTime,etFromTime,etNumber;
    private TextView tvError;
    private Button btnSave;
    private List<String> alreadyPeriodList=new ArrayList<>();
    private List<String> alreadyFromTimeList=new ArrayList<>();
    private List<String> alreadyToTimeList=new ArrayList<>();
    private String periodNumber, fromTime, toTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        String userJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Staff.class);
        instituteId = sessionManager.getString("instituteId");
        academicYearId=sessionManager.getString("academicYearId");
        pDialog=Utility.createSweetAlertDialog(getContext());
    }

    public FragmentPeriod() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_period, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.period_cap));
        spBatch = view.findViewById(R.id.spBatch);
        rvPeriod = (RecyclerView) view.findViewById(R.id.rvPeriod);
        layoutManager = new LinearLayoutManager(getContext());
        rvPeriod.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        createBottomSheet();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addPeriod);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
            }});

        getBatches();
        return view;
    }
    BottomSheetDialog bottomSheetDialog;
    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_period, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            tvError = view.findViewById(R.id.tvError);
            etNumber = view.findViewById(R.id.etNumber);
            etFromTime = view.findViewById(R.id.etFromTime);
            etToTime = view.findViewById(R.id.etToTime);
            btnSave = view.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    periodNumber = etNumber.getText().toString().trim();
                    if (TextUtils.isEmpty(periodNumber)) {
                        etNumber.setError("Enter period number");
                        etNumber.requestFocus();
                        return;
                    }
                    fromTime = etFromTime.getText().toString().trim();
                    if (TextUtils.isEmpty(fromTime)) {
                        etFromTime.setError("Enter from time");
                        etFromTime.requestFocus();
                        return;
                    }
                    toTime = etToTime.getText().toString().trim();
                    if (TextUtils.isEmpty(toTime)) {
                        etToTime.setError("Enter to time");
                        etToTime.requestFocus();
                        return;
                    }

                    String fromTime24=convertTime12to24(fromTime);
                    String toTime24=convertTime12to24(toTime);

                    if(fromTime24.equalsIgnoreCase("E")){
                        etFromTime.setError("Invalid from time");
                        etFromTime.requestFocus();
                        return;
                    }
                    if(toTime24.equalsIgnoreCase("E")){
                        etToTime.setError("Invalid to time");
                        etToTime.requestFocus();
                        return;
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

                    // Parsing the Time Period
                    Date date1 = null, date2 = null;
                    try {
                        date1 = simpleDateFormat.parse(fromTime24);
                        date2 = simpleDateFormat.parse(toTime24);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(date1.equals(date2) )
                    {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Period 'From' Time cannot be same as Period 'To' Time.");
                        return;
                    }
                    else if(date2.before(date1))
                    {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("'To' time should not be less then 'From' time.");
                        return;
                    }
                    String[] from= fromTime24.split(":");
                    String[] to = toTime24.split(":");
                    int duration=(Integer.parseInt(to[0])-Integer.parseInt(from[0]))*60;
                    duration=duration+(Integer.parseInt(to[1])-Integer.parseInt(from[1]));

                    String Period_Number=period.getNumber();
                    Period_Number=Period_Number.toUpperCase();
                    Period_Number=Period_Number.replaceAll("\\s+", "");
                    String EditNumber=periodNumber.toUpperCase();
                    EditNumber=EditNumber.replaceAll("\\s+", "");
                    if(Period_Number != EditNumber){
                        for(int i=0;i<alreadyPeriodList.size();i++){
                            String periodNumber=alreadyPeriodList.get(i).toUpperCase();
                            periodNumber=periodNumber.replaceAll("\\s+", "");
                            if(EditNumber == periodNumber){
                                tvError.setVisibility(View.VISIBLE);
                                tvError.setText("Already this period number is present.");
                                break;
                            }
                        }
                    }

                    String Period_FromTime=period.getFromTime();
                    Period_FromTime=Period_FromTime.toUpperCase();
                    Period_FromTime=Period_FromTime.replaceAll("\\s+", "");
                    String EditFromTime=fromTime.toUpperCase();
                    EditFromTime=EditFromTime.replaceAll("\\s+", "");
                    if(Period_FromTime != EditFromTime){
                        for(int i=0;i<alreadyFromTimeList.size();i++){
                            String periodFromTime=alreadyFromTimeList.get(i).toUpperCase();
                            periodFromTime=periodFromTime.replaceAll("\\s+", "");
                            if(EditFromTime == periodFromTime){
                                tvError.setVisibility(View.VISIBLE);
                                tvError.setText("Already this period from time is present");
                                break;
                            }
                        }
                    }

                    String Period_ToTime=period.getToTime();
                    Period_ToTime=Period_ToTime.toUpperCase();
                    Period_ToTime=Period_ToTime.replaceAll("\\s+", "");
                    String EditToTime=toTime.toUpperCase();
                    EditToTime=EditToTime.replaceAll("\\s+", "");
                    if(Period_ToTime != EditToTime){
                        for(int i=0;i<alreadyToTimeList.size();i++){
                            String periodToTime=alreadyToTimeList.get(i).toUpperCase();
                            periodToTime=periodToTime.replaceAll("\\s+", "");
                            if(EditToTime == periodToTime){
                                tvError.setVisibility(View.VISIBLE);
                                tvError.setText("Already this period to time is present.");
                                break;
                            }
                        }
                    }
                    if (pDialog == null && !pDialog.isShowing()) {
                        pDialog.show();
                    }
                    period = new Period();
                    period.setBatchId(selectedBatch.getId());
                    period.setNumber(periodNumber);
                    period.setFromTime(fromTime);
                    period.setToTime(toTime);
                    period.setDuration(duration);
                    period.setCreatorId(loggedInUser.getId());
                    period.setModifierId(loggedInUser.getId());
                    addPeriod();
                }
            });
        }
    }
    private void addPeriod(){
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Period successfully added")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        periodCollectionRef
                                .add(period)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        bottomSheetDialog.dismiss();
                                        getPeriodsOfBatch();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.w(TAG, "Error adding document", e);
                                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                                    }
                                });
                        // [END add_document]
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    private void getBatches() {
        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            batch = document.toObject(Batch.class);
                            batch.setId(document.getId());
                            batchList.add(batch);
                        }
                        System.out.println("batchList ==> "+batchList.size());
                        if (batchList.size()!=0) {
                            List<String> batchNameList = new ArrayList<>();
                            for (Batch batch : batchList) {
                                batchNameList.add(batch.getName());
                            }
                            ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                            batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spBatch.setAdapter(batchAdaptor);
                            spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (periodList != null) {
                                        periodList.clear();
                                    }
                                    selectedBatch = batchList.get(position);
                                    selectedBatchPeriod = gson.toJson(selectedBatch);
                                    getPeriodsOfBatch();
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            spBatch.setEnabled(false);
                        }


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        spBatch.setEnabled(false);
                    }
                });
    }
    private void getPeriodsOfBatch() {
        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if(alreadyPeriodList.size()>0){
            alreadyPeriodList.clear();
        }
        if(periodList.size()>0){
            periodList.clear();
        }
        if(alreadyFromTimeList.size()>0){
            alreadyFromTimeList.clear();
        }
        if(alreadyToTimeList.size()>0){
            alreadyToTimeList.clear();
        }
        periodCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .orderBy("fromTime", Query.Direction.ASCENDING)
                .orderBy("toTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            alreadyPeriodList.add(period.getNumber());
                            alreadyFromTimeList.add(period.getFromTime());
                            alreadyToTimeList.add(period.getToTime());
                            periodList.add(period);
                        }
                        if (periodList.size()!=0) {
                            System.out.println("periodList = "+periodList.size());
                            rvPeriod.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            periodAdapter = new PeriodAdapter(periodList);
                            periodAdapter.notifyDataSetChanged();
                            rvPeriod.setAdapter(periodAdapter);
                        } else {
                            rvPeriod.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }

                    }
                });
    }
    class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.MyViewHolder> {
        private List<Period> periodList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvNumber, tvFromTime, tvToTime, tvDuration;
            public ImageView ivEditPeriod,ivDeletePeriod;

            public MyViewHolder(View view) {
                super(view);
                tvNumber = view.findViewById(R.id.tvNumber);
                tvFromTime = view.findViewById(R.id.tvFromTime);
                tvToTime = view.findViewById(R.id.tvToTime);
                tvDuration = view.findViewById(R.id.tvDuration);
                ivEditPeriod = view.findViewById(R.id.ivEditPeriod);
                ivDeletePeriod = view.findViewById(R.id.ivDeletePeriod);
            }
        }


        public PeriodAdapter(List<Period> periodList) {
            this.periodList = periodList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_period, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Period period = periodList.get(position);

            holder.tvNumber.setText("" + period.getNumber());
            holder.tvFromTime.setText(""+ period.getFromTime());
            holder.tvToTime.setText("" + period.getToTime());
            holder.tvDuration.setText(""+period.getDuration()+" min");

            holder.ivDeletePeriod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("period.id "+period.getId());
                    deletePeriod(period);
                }
            });
            holder.ivEditPeriod.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    editPeriod(period);
                }
            });
        }

        @Override
        public int getItemCount() {
            return periodList.size();
        }
    }
    private void editPeriod(Period period){
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_edit_period, null);
        TextView tvError = dialogLayout.findViewById(R.id.tvError);
        EditText etPeriodNumber = dialogLayout.findViewById(R.id.etPeriodNumber);
        etPeriodNumber.setText("" + period.getNumber());
        EditText etFromTime = dialogLayout.findViewById(R.id.etFromTime);
        etFromTime.setText("" + period.getFromTime());
        EditText etToTime = dialogLayout.findViewById(R.id.etToTime);
        etToTime.setText("" + period.getToTime());
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Edit Period")
                .setConfirmText("Update")
                .setCustomView(dialogLayout)
                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        tvError.setVisibility(View.GONE);
                        periodNumber = etPeriodNumber.getText().toString().trim();
                        if (TextUtils.isEmpty(periodNumber)) {
                            etPeriodNumber.setError("Enter period name");
                            etPeriodNumber.requestFocus();
                            return;
                        }
                        fromTime = etFromTime.getText().toString().trim();
                        if (TextUtils.isEmpty(fromTime)) {
                            etFromTime.setError("Enter from time");
                            etFromTime.requestFocus();
                            return;
                        }
                        toTime = etToTime.getText().toString().trim();
                        if (TextUtils.isEmpty(toTime)) {
                            etToTime.setError("Enter to time");
                            etToTime.requestFocus();
                            return;
                        }
                        String fromTime24=convertTime12to24(fromTime);
                        String toTime24=convertTime12to24(toTime);

                        if(fromTime24.equalsIgnoreCase("E")){
                            etFromTime.setError("Invalid from time");
                            etFromTime.requestFocus();
                            return;
                        }
                        if(toTime24.equalsIgnoreCase("E")){
                            etToTime.setError("Invalid to time");
                            etToTime.requestFocus();
                            return;
                        }
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

                        // Parsing the Time Period
                        Date date1 = null, date2 = null;
                        try {
                            date1 = simpleDateFormat.parse(fromTime24);
                            date2 = simpleDateFormat.parse(toTime24);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(date1.equals(date2))
                        {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("Period 'From' Time cannot be same as Period 'To' Time.");
                            return;
                        }
                        else if(date2.before(date1))
                        {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("'To' time should not be less then 'From' time.");
                            return;
                        }
                        String[] from= fromTime24.split(":");
                        String[] to = toTime24.split(":");
                        int editDuration=(Integer.parseInt(to[0])-Integer.parseInt(from[0]))*60;
                        editDuration=editDuration+(Integer.parseInt(to[1])-Integer.parseInt(from[1]));

                        String Period_Number=period.getNumber();
                        Period_Number=Period_Number.toUpperCase();
                        Period_Number=Period_Number.replaceAll("\\s+", "");
                        String EditNumber=periodNumber.toUpperCase();
                        EditNumber=EditNumber.replaceAll("\\s+", "");
                        if(Period_Number != EditNumber){
                            for(int i=0;i<alreadyPeriodList.size();i++){
                                String periodNumber=alreadyPeriodList.get(i).toUpperCase();
                                periodNumber=periodNumber.replaceAll("\\s+", "");
                                if(EditNumber == periodNumber){
                                    tvError.setVisibility(View.VISIBLE);
                                    tvError.setText("Already this period number is present.");
                                    break;
                                }
                            }
                        }

                        String Period_FromTime=period.getFromTime();
                        Period_FromTime=Period_FromTime.toUpperCase();
                        Period_FromTime=Period_FromTime.replaceAll("\\s+", "");
                        String EditFromTime=fromTime.toUpperCase();
                        EditFromTime=EditFromTime.replaceAll("\\s+", "");
                        if(Period_FromTime != EditFromTime){
                            for(int i=0;i<alreadyFromTimeList.size();i++){
                                String periodFromTime=alreadyFromTimeList.get(i).toUpperCase();
                                periodFromTime=periodFromTime.replaceAll("\\s+", "");
                                if(EditFromTime == periodFromTime){
                                    tvError.setVisibility(View.VISIBLE);
                                    tvError.setText("Already this period from time is present");
                                    break;
                                }
                            }
                        }

                        String Period_ToTime=period.getToTime();
                        Period_ToTime=Period_ToTime.toUpperCase();
                        Period_ToTime=Period_ToTime.replaceAll("\\s+", "");
                        String EditToTime=toTime.toUpperCase();
                        EditToTime=EditToTime.replaceAll("\\s+", "");
                        if(Period_ToTime != EditToTime){
                            for(int i=0;i<alreadyToTimeList.size();i++){
                                String periodToTime=alreadyToTimeList.get(i).toUpperCase();
                                periodToTime=periodToTime.replaceAll("\\s+", "");
                                if(EditToTime == periodToTime){
                                    tvError.setVisibility(View.VISIBLE);
                                    tvError.setText("Already this period to time is present.");
                                    break;
                                }
                            }
                        }

                        period.setNumber(periodNumber);
                        period.setFromTime(fromTime);
                        period.setToTime(toTime);
                        period.setDuration(editDuration);
                        period.setModifiedDate(new Date());
                        period.setModifierId(loggedInUser.getId());

                        periodCollectionRef
                                .document(period.getId())
                                .set(period)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        sDialog.dismissWithAnimation();
                                        Toast.makeText(getContext(),"Error",Toast.LENGTH_LONG);
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sDialog.dismissWithAnimation();
                                        getPeriodsOfBatch();
                                    }
                                });
                    }
                });
        dialog.setCancelable(false);
        dialog.show();
    }
    private void deletePeriod(Period selectedPeriod){
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        timeTableCollectionRef
                .whereEqualTo("periodId", selectedPeriod.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (documentSnapshots.size() == 0) {
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Delete")
                                    .setContentText("Do you want to delete " + selectedPeriod.getNumber() + "?")
                                    .setConfirmText("Confirm")
                                    .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                        }
                                    })
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            if (pDialog != null && !pDialog.isShowing()) {
                                                pDialog.show();
                                            }
                                            periodCollectionRef.document(period.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Period has been deleted.")
                                                                    .setConfirmText("Ok");
                                                            dialog.setCancelable(false);
                                                            dialog.show();
                                                            getPeriodsOfBatch();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                                    .setTitleText("Unable to delete Period")
                                                                    .setContentText("For some network issue please check it.")
                                                                    .setConfirmText("Ok");
                                                            dialog.setCancelable(false);
                                                            dialog.show();
                                                            getPeriodsOfBatch();
                                                        }
                                                    });

                                            sDialog.dismissWithAnimation();
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Unable to delete Period")
                                    .setContentText("In this Period some timetables are there.")
                                    .setConfirmText("Ok");
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        System.out.println("Error getting documents:" + e);
                    }
                });
    }

    private String convertTime12to24(String time12h) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        Date date=null;
        try {
            date = parseFormat.parse(time12h);
        }catch (Exception e){
            return "E";
        }
        return displayFormat.format(date);
    }
}
