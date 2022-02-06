package com.padmajeet.mgi.techforedu.admin;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.admin.model.Batch;
import com.padmajeet.mgi.techforedu.admin.model.Enquiry;
import com.padmajeet.mgi.techforedu.admin.model.Parent;
import com.padmajeet.mgi.techforedu.admin.model.Student;
import com.padmajeet.mgi.techforedu.admin.util.SessionManager;
import com.padmajeet.mgi.techforedu.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddEditViewStudent extends Fragment {
    private String usn, firstName, lastName, mobileNumber, father, joiningYear,mother,fatherMobileNumber,motherMobileNumber,emergencyContactNumber,address,emailId,uniformSize,shoeSize;
    private EditText etUSN, etFirstName, etLastName, etMobileNumber, etDob, etJoiningYear,etFatherFirstName,etFatherMiddleName,etFatherMobileNumber,etEmailId,etUniformSize,etShoeSize;
    private EditText etMotherMobileNumber,etEmergencyContact,etAddress,etAmount,etReceiptId;
    private TextView tvError;
    private String paymentOption,fatherFirstName,fatherMiddleName;
    private Button btnSave;
    private DatePickerDialog picker;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SweetAlertDialog pDialog;
    private View view = null;
    private String loggedInUserId;
    private Batch selectedBatch;
    private RadioButton radioBtnMale, radioBtnFemale;
    private RadioGroup radioGroupGender;
    private String gender;
    private ImageView ivDob;
    private Spinner spPaymentOption;
    private Date dob;
    private Student student = new Student();
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference parentCollectionRef=db.collection("Parent");
    private CollectionReference studentCollectionRef=db.collection("Student");
    //private CollectionReference studentKitCollectionRef=db.collection("StudentKit");
    //private CollectionReference studentFeesCollectionRef=db.collection("StudentFees");
    //private CollectionReference sectionCollectionRef=db.collection("Section");
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private DocumentReference batchDocRef;
    //private StudentFees studentFees;
    private ImageView ivProfilePic;
    private ImageButton ibChoosePhoto;
    private final int PICK_IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageReference storageReference;
    private String imageUrl;
    private StorageTask mUploadTask;
    //private StudentKit studentKit;
    private Enquiry selectedEnquiry;
    private TextView tvSelectedBatch;
    private Parent parent;
    //private Spinner spSection;
    //private Section section;
    //private List<Section> sectionList=new ArrayList<>();
    //private String sectionId;
    private Spinner spBatch;
    private Batch batch;
    private List<Batch> batchList=new ArrayList<>();
    private String batchId;
    private String instituteId,academicYearId;
    private Gson gson;
    float amount;
    private String receiptId;
    private Fragment currentFragment;
    private StudentParent selectedStudentParent;
    private String selectedBatchId;
    private boolean isAddMode;
    private Button btnUpdate;

    private class StudentParent{
        private Student student;
        private Parent parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        instituteId=sessionManager.getString("instituteId");
        academicYearId= sessionManager.getString("academicYearId");
        pDialog= Utility.createSweetAlertDialog(getContext());
    }

    public FragmentAddEditViewStudent() {
        // Required empty public constructor
        currentFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_edit_view_student, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.student));

        Bundle bundle = getArguments();
        String selectedBatchStudentJson = bundle.getString("selectedBatchStudent");
       // System.out.println("selectedBatchStudentJson - " + selectedBatchStudentJson);
        gson = Utility.getGson();
        selectedBatch = gson.fromJson(selectedBatchStudentJson, Batch.class);

        String selectedEnquiryJson = bundle.getString("selectEnquiry");
       // System.out.println("selectEnquiry - " + selectedEnquiryJson);
        selectedEnquiry = gson.fromJson(selectedEnquiryJson, Enquiry.class);

        storageReference= FirebaseStorage.getInstance().getReference("Profile");

        //spSection=view.findViewById(R.id.spSection);
        btnUpdate= view.findViewById(R.id.btnUpdate);
        spPaymentOption= view.findViewById(R.id.spPaymentOption);
        tvError=view.findViewById(R.id.tvError);
        btnSave = view.findViewById(R.id.btnSave);
        spBatch=view.findViewById(R.id.spBatch);
        etUSN = view.findViewById(R.id.etUSN);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobileNumber = view.findViewById(R.id.etMobileNumber);
        etJoiningYear = view.findViewById(R.id.etJoiningYear);
        radioBtnFemale = view.findViewById(R.id.radioBtnFemale);
        radioBtnMale = view.findViewById(R.id.radioBtnMale);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        ivDob = view.findViewById(R.id.ivDob);
        etDob = view.findViewById(R.id.etDob);
        ivProfilePic=view.findViewById(R.id.ivProfilePic);
        ibChoosePhoto=view.findViewById(R.id.ibChoosePhoto);
        //etMotherMobileNumber = view.findViewById(R.id.etMotherMobileNumber);
        etEmergencyContact = view.findViewById(R.id.etEmergencyContact);
        etAddress = view.findViewById(R.id.etAddress);
       // btnUpload=view.findViewById(R.id.btnUpload);
        etDob.setInputType(InputType.TYPE_NULL);
        etFatherFirstName=view.findViewById(R.id.etFatherFirstName);
        etFatherMiddleName=view.findViewById(R.id.etFatherMiddleName);
        etFatherMobileNumber=view.findViewById(R.id.etFatherMobileNumber);
        etEmailId=view.findViewById(R.id.etEmailId);
       // etRelation=view.findViewById(R.id.etRelation);
        /*tvSelectedBatch=view.findViewById(R.id.tvSelectedBatch);
        etAmount = view.findViewById(R.id.etAmount);
        etReceiptId = view.findViewById(R.id.etReceiptId);
        etUniformSize=view.findViewById(R.id.etUniformSize);
        etShoeSize=view.findViewById(R.id.etShoeSize);*/
        final Calendar cal = Calendar.getInstance();
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        final int month = cal.get(Calendar.MONTH);
        final int year = cal.get(Calendar.YEAR);
        if(getArguments()==null){//Add Mode
            if(selectedEnquiry!=null){
                etDob.setEnabled(true);
                etFirstName.setText(""+selectedEnquiry.getFirstName());
                etLastName.setText(""+selectedEnquiry.getLastName());
                Date birthDay=selectedEnquiry.getDob();
                cal.setTime(birthDay);
                etDob.setText(String.format("%02d",cal.get(Calendar.DAY_OF_MONTH))+"/"+String.format("%02d",(cal.get(Calendar.MONTH)+1))+"/"+String.format("%02d",cal.get(Calendar.YEAR)));
            /*
            batchDocRef = db.document("Batch/" + selectedEnquiry.getBatchId());
            batchDocRef
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            selectedBatch=documentSnapshot.toObject(Batch.class);
                            selectedBatch.setId(documentSnapshot.getId());
                            //System.out.println("BatchId - "+selectedBatch.getName());
                            tvSelectedBatch.setText(""+selectedBatch.getName());
                            getSection();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            */
                gender = selectedEnquiry.getGender();
                if(gender.equalsIgnoreCase("Male")){
                    radioBtnMale.setChecked(true);
                }
                else{
                    radioBtnFemale.setChecked(true);
                }
                etFatherFirstName.setText(""+selectedEnquiry.getFather());
                //etMiddleName.setText(""+selectedEnquiry.getFather());
                etFatherMobileNumber.setText(""+selectedEnquiry.getMobileNumber());
                if(!TextUtils.isEmpty(selectedEnquiry.getEmailId())){
                    etEmailId.setText(""+selectedEnquiry.getEmailId());
                }
                etAddress.setText(selectedEnquiry.getAddress());
            }
            else{
                etFirstName.setText("");
                //etMiddleName.setText("");
                etLastName.setText("");
                etFatherFirstName.setText("");
                etFatherMiddleName.setText("");
                etFatherMobileNumber.setText("");
                etEmailId.setText("");
            /*if(selectedBatch!=null) {
                tvSelectedBatch.setText("" + selectedBatch.getName());
                getSection();
            }*/
            }
        }
        else{//Edit Mode
            String selectedStudentParentJson = getArguments().getString("selectedStudentParent");
            btnUpdate.setVisibility(View.GONE);
            selectedStudentParent= gson.fromJson(selectedStudentParentJson,StudentParent.class);
            selectedBatchId = selectedStudentParent.student.getCurrentBatchId();
            isAddMode = false;
            setStudentData();
            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        }
        getBatches();
        Date today=new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(today);
        etJoiningYear.setText(""+calendar.get(Calendar.YEAR));
        etJoiningYear.setEnabled(true);
        ibChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();

            }
        });
        ivDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // date picker dialog
                picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDob.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(new Date().getTime());
                picker.setTitle("Select Date of Birth");
                picker.show();
            }
        });
        etDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // date picker dialog
                picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDob.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(new Date().getTime());
                picker.setTitle("Select Date of Birth");
                picker.show();
            }
        });
        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioBtnMale.isChecked()) {
                    gender="Male";
                } else {
                    if (radioBtnFemale.isChecked()) {
                        gender="Female";
                    }
                }

            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.payment_option_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spPaymentOption.setAdapter(adapter);
        spPaymentOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                paymentOption=spPaymentOption.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });


        return view;
    }

    private void setStudentData(){
        imageUrl = selectedStudentParent.student.getImageUrl();
        System.out.println("setStaffData imageUrl "+imageUrl);
        if (imageUrl != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_professor_64_01)
                    .into(ivProfilePic);
        }
        etUSN.setText(""+selectedStudentParent.student.getUsn());
        etFirstName.setText(""+selectedStudentParent.student.getFirstName());
        etLastName.setText(""+selectedStudentParent.student.getLastName());
        gender = selectedStudentParent.student.getGender();
        if(gender.equalsIgnoreCase("Male")){
            radioBtnMale.setChecked(true);
        }
        else{
            radioBtnFemale.setChecked(true);
        }
        if(selectedStudentParent.student.getDob()!=null) {
            String dob = dateFormat.format(selectedStudentParent.student.getDob());
            etDob.setText(""+dob);
        }
        etEmailId.setText(""+selectedStudentParent.student.getEmailId());
        if(selectedStudentParent.student.getJoiningYear() != 0) {
            etJoiningYear.setText("" + selectedStudentParent.student.getJoiningYear());
        }
        etMobileNumber.setText(""+selectedStudentParent.student.getMobileNumber());
        etEmergencyContact.setText(""+selectedStudentParent.student.getMobileNumber());
        etFatherFirstName.setText(""+selectedStudentParent.parent.getFirstName());
        etFatherMiddleName.setText(""+selectedStudentParent.parent.getMiddleName());
        etFatherMobileNumber.setText(""+selectedStudentParent.parent.getMobileNumber());
        etAddress.setText(""+selectedStudentParent.student.getAddress());
    }

    private void getBatches(){
        if(batchList.size()!=0){
            batchList.clear();
        }

        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                batch = document.toObject(Batch.class);
                                batch.setId(document.getId());
                                batchList.add(batch);
                            }
                            if(batchList.size()!=0) {
                                List<String> batchNameList = new ArrayList<>();
                                for (Batch batch : batchList) {
                                    batchNameList.add(batch.getName());
                                }
                                ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spBatch.setAdapter(sectionAdaptor);

                                spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                        batchId = batchList.get(position).getId();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }else{
                                spBatch.setEnabled(false);
                                btnSave.setVisibility(View.GONE);
                                tvError.setText(R.string.noBatch);
                            }
                        } else {
                            spBatch.setEnabled(false);
                        }
                    }
                });
        // [END get_all_users]

    }

    private void showFileChooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select an image"),PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void  uploadFile(){
        if(imageUri!=null) {
            final ProgressDialog progressDialog=new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final StorageReference fileRef = storageReference.child(selectedBatch.getName()+System.currentTimeMillis()+"."+getFileExtension(imageUri));

            mUploadTask=fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress(0);
                                }
                            },5000);
                            Toast.makeText(getContext(),"Uploaded..",Toast.LENGTH_LONG).show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                           // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    imageUrl = downloadUrl.toString();
                                    //System.out.println("ImageURL -"+imageUrl);
                                    if (imageUrl != null) {
                                        Glide.with(getContext())
                                                .load(imageUrl)
                                                .fitCenter()
                                                .apply(RequestOptions.circleCropTransform())
                                                .placeholder(R.drawable.ic_student_71)
                                                .into(ivProfilePic);
                                    }
                                    addStudent();
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(((int)progress)+"% Uploaded...");
                        }
                    });
        }else {
            Toast.makeText(getContext(),"No file selected",Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void validateForm(){
        usn = etUSN.getText().toString().trim();
        if (TextUtils.isEmpty(usn)) {
            etUSN.setError("Enter usn");
            etUSN.requestFocus();
            return;
        }else{
            if(Utility.isNumericWithSpace(usn)){
                etUSN.setError("USN must be alphanumeric");
                etUSN.requestFocus();
                return;
            }
        }
        firstName = etFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("Enter first name");
            etFirstName.requestFocus();
            return;
        }else{
            if(!Utility.isAlphabetic(firstName)){
                etFirstName.setError("First name must be alphabetic");
                etFirstName.requestFocus();
                return;
            }
        }
        lastName = etLastName.getText().toString().trim();
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Enter last name");
            etLastName.requestFocus();
            return;
        }else{
            if(!Utility.isAlphabetic(lastName)){
                etLastName.setError("Last name must be alphabetic");
                etLastName.requestFocus();
                return;
            }
        }
        String DOB = etDob.getText().toString().trim();
        if (TextUtils.isEmpty(DOB)) {
            etDob.setError("Enter Date Of Birth");
            etDob.requestFocus();
            return;
        }else {
            try {
                dob = dateFormat.parse(DOB);
            } catch (ParseException e) {
                etDob.setError("DD/MM/YYYY");
                etDob.requestFocus();
                e.printStackTrace();
            }
        }
        emailId = etEmailId.getText().toString().trim();
        if(!TextUtils.isEmpty(emailId)){
            if(!Utility.isEmailValid(emailId)){
                etEmailId.setError("Enter valid email id");
                etEmailId.requestFocus();
                return;
            }
        }
        joiningYear = etJoiningYear.getText().toString().trim();
        if (TextUtils.isEmpty(joiningYear)) {
            etJoiningYear.setError("Enter joining year");
            etJoiningYear.requestFocus();
            return;
        }else{
            if(!Utility.isYear(joiningYear)){
                etJoiningYear.setError("Enter valid joining year, it must be 4 digits");
                etJoiningYear.requestFocus();
                return;
            }
        }
        mobileNumber = etMobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length()>10 ||mobileNumber.length()<10) {
            etMobileNumber.setError("Enter 10 digit's mobile number");
            etMobileNumber.requestFocus();
            return;
        }else{
            if (!Utility.isValidPhone(mobileNumber)) {
                etMobileNumber.setError("Enter valid mobile number");
                etMobileNumber.requestFocus();
                return;
            }
        }
        emergencyContactNumber = etEmergencyContact.getText().toString().trim();
        if (TextUtils.isEmpty(emergencyContactNumber) || emergencyContactNumber.length()>10 ||emergencyContactNumber.length()<10) {
            etEmergencyContact.setError("Enter 10 digit's mobile number");
            etEmergencyContact.requestFocus();
            return;
        }else{
            if (!Utility.isValidPhone(emergencyContactNumber)) {
                etEmergencyContact.setError("Enter valid mobile number");
                etEmergencyContact.requestFocus();
                return;
            }
        }
        fatherFirstName = etFatherFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(fatherFirstName)) {
            etFatherFirstName.setError("Enter father's first name");
            etFatherFirstName.requestFocus();
            return;
        }else{
            if(!Utility.isAlphabetic(fatherFirstName)){
                etFatherFirstName.setError("Father's first name must be alphabetic");
                etFatherFirstName.requestFocus();
                return;
            }
        }
        fatherMiddleName = etFatherMiddleName.getText().toString().trim();
        if (!TextUtils.isEmpty(fatherMiddleName)) {
            if(!Utility.isAlphabetic(fatherMiddleName)){
                etFatherMiddleName.setError("Father's middle name must be alphabetic");
                etFatherMiddleName.requestFocus();
                return;
            }
        }
        fatherMobileNumber = etFatherMobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(fatherMobileNumber) || fatherMobileNumber.length()>10 ||fatherMobileNumber.length()<10) {
            etFatherMobileNumber.setError("Enter 10 digit's mobile number");
            etFatherMobileNumber.requestFocus();
            return;
        }else{
            if (!Utility.isValidPhone(fatherMobileNumber)) {
                etFatherMobileNumber.setError("Enter valid mobile number");
                etFatherMobileNumber.requestFocus();
                return;
            }
        }
        address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Enter address");
            etAddress.requestFocus();
            return;
        }else{
            if(Utility.isNumericWithSpace(address)){
                etAddress.setError("Enter valid address");
                etAddress.requestFocus();
                return;
            }
        }
        System.out.println("selectedBatchId - "+selectedBatchId);
        selectedStudentParent.student.setInstituteId(instituteId);
        selectedStudentParent.student.setCurrentBatchId(selectedBatchId);
        selectedStudentParent.student.setFirstName(firstName);
        selectedStudentParent.student.setLastName(lastName);
        selectedStudentParent.student.setGender(gender);
        selectedStudentParent.student.setDob(dob);
        selectedStudentParent.student.setEmailId(emailId);
        selectedStudentParent.student.setJoiningYear(Integer.parseInt(joiningYear));
        selectedStudentParent.student.setMobileNumber(mobileNumber);
        //selectedStudentParent.parent.(emergencyContact);
        selectedStudentParent.student.setAddress(address);
        selectedStudentParent.parent.setFirstName(fatherFirstName);
        selectedStudentParent.parent.setMiddleName(fatherMiddleName);
        selectedStudentParent.parent.setMobileNumber(fatherMobileNumber);
        selectedStudentParent.parent.setAddress(address);
        selectedStudentParent.student.setModifierId(loggedInUserId);
        selectedStudentParent.student.setModifierType("A");
        selectedStudentParent.student.setModifiedDate(new Date());
        selectedStudentParent.parent.setModifierId(loggedInUserId);
        selectedStudentParent.parent.setModifierType("A");
        selectedStudentParent.parent.setModifiedDate(new Date());
        if(imageUri == null){
            if(isAddMode){
                addStudent();
            }
            else {
                updateStudent();
            }
        }else{
            uploadFile();
        }
    }

    private void updateEnquiry(){

        selectedEnquiry.setStatus("A");
        CollectionReference enquiryCollectionRef = db.collection("Enquiry");
        enquiryCollectionRef.document(selectedEnquiry.getId()).set(selectedEnquiry).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }
    private void addStudentFees(){
        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        /*
        studentFeesCollectionRef
                .add(studentFees)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }

                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                    }
                });
        // [END add_document]
        */
    }
    private void addStudentKit(){
        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        /*
        studentKitCollectionRef
                .add(studentKit)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }

                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                    }
                });
        // [END add_document]
        */
    }

    private void addParent(){
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        parentCollectionRef
                .add(parent)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }

                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                    }
                });
        // [END add_document]

    }

    private void updateStudent(){
        if(pDialog!=null) {
            pDialog.show();
        }
        parentCollectionRef
                .document(selectedStudentParent.parent.getId())
                .set(selectedStudentParent.parent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                    }
                });
        studentCollectionRef
                .document(selectedStudentParent.student.getId())
                .set(selectedStudentParent.student)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Student successfully updated")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                    }
                });
    }

    private void addStudent() {
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        studentCollectionRef
                .add(selectedStudentParent.student)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        parent.setStudentId(documentReference.getId());
                        addParent();
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Student successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //Toast.makeText(getContext(), "Student is successfully added", Toast.LENGTH_SHORT).show();
                                        FragmentStudent fragmentStudent = new FragmentStudent();
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        fragmentTransaction.replace(R.id.contentLayout, fragmentStudent).addToBackStack(null).commit();

                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END add_document]

    }


}
