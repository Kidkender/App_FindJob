package com.android.app_findjob.view.home.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app_findjob.R;
import com.android.app_findjob.databinding.ActivityDetailJobBinding;
import com.android.app_findjob.model.Job;
import com.android.app_findjob.model.JobActive;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailJobActivity extends AppCompatActivity {
    private ActivityDetailJobBinding binding ;
    private ArrayList<JobActive> list;
    private int idJobIntent;
    private boolean checkHeart = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        binding.btnBack.setOnClickListener(view -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        Intent intent = getIntent();
        int idEmployerIntent = intent.getIntExtra("IDEmployer",-1);
        idJobIntent = intent.getIntExtra("IDJob",-1);
        String imgEmployerIntent = intent.getStringExtra("ImgEmployer");
        String nameEmployerIntent = intent.getStringExtra("NameEmployer");

        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        getListDataFirebase(userAuth.getUid());

        binding.layoutTop.setOnClickListener(view -> {
            Intent i = new Intent(this, DetailEmployerActivity.class);
            Bundle mBundle  = new Bundle();
            mBundle.putInt("IDEmployer",idEmployerIntent);
            i.putExtras(mBundle);
            startActivity(i);
        });
        binding.btnApply.setOnClickListener(view -> {
            showDialogApply();
        });



        Picasso.get()
                .load(imgEmployerIntent)
                .into(binding.imgViewEmployer);
        binding.txtNameEmployer.setText(nameEmployerIntent);

        DatabaseReference mDatabaseEmployer = FirebaseDatabase.getInstance().getReference("Job/" + idJobIntent);
        mDatabaseEmployer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshotEmployer) {
                Job job = (Job) dataSnapshotEmployer.getValue(Job.class);
                binding.txtNameJob.setText(job.getName());
                binding.txtSalaryJob.setText(job.getSalary());
                binding.txtVacanciesJob.setText(job.getVacancies());
                binding.txtSkillJob.setText(job.getSkill());
                binding.txtAddressJob.setText(job.getAddress());
                binding.txtTime.setText(job.getTime());

                binding.txtdes1.setText(job.getDescribe().getDes1());
                binding.txtdes2.setText(job.getDescribe().getDes2());
                binding.txtdes3.setText(job.getDescribe().getDes3());
                binding.txtreq1.setText(job.getRequest().getReq1());
                binding.txtreq2.setText(job.getRequest().getReq2());
                binding.txtreq3.setText(job.getRequest().getReq3());
                binding.txtreq4.setText(job.getRequest().getReq4());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        binding.btnHeart.setOnClickListener(view -> {
            DatabaseReference mDatabaseFollow = FirebaseDatabase.getInstance().getReference("UserActivity").child(userAuth.getUid()).child("JobFollow");
            if (!checkHeart) {
                binding.btnHeart.setImageResource(R.drawable.heart1);
                checkHeart = true;
                JobActive mJobActive = new JobActive();
                Boolean check = false ;
                for (JobActive e : list) {
                    if (e.getJobID() == idJobIntent) {
                        mJobActive.setId(e.getId());
                        check = true ;
                    }
                }
                if(!check){
                    if (list.size() == 0) {
                        mJobActive.setId(0);
                    } else {
                        mJobActive.setId(list.get(list.size() - 1).getId() + 1);
                    }
                }
                mJobActive.setJobID(idJobIntent);
                mDatabaseFollow.child(mJobActive.getId() + "").setValue(mJobActive);
                list.add(mJobActive);
                Toast.makeText(this,"Đã thêm công việc này vào mục yêu thích",Toast.LENGTH_SHORT).show();
                return;
            } else {
                binding.btnHeart.setImageResource(R.drawable.heart);
                checkHeart = false;
                Toast.makeText(this,"Đã xóa công việc này khỏi mục yêu thích",Toast.LENGTH_SHORT).show();
                for (JobActive jobActive : list) {
                    if (jobActive.getJobID() == idJobIntent) {
                        mDatabaseFollow.child(jobActive.getId()+"").removeValue();
                        list.remove(jobActive);
                        return;
                    }
                }
            }
        });
    }

    private void init(){
        binding=ActivityDetailJobBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private void getListDataFirebase(String idUser) {
        list = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("UserActivity").child(idUser).child("JobFollow");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    JobActive jobActive = (JobActive) postSnapshot.getValue(JobActive.class);
                    if (jobActive.getJobID() == idJobIntent) {
                        binding.btnHeart.setImageResource(R.drawable.heart1);
                        checkHeart = true;
                    }
                    list.add(jobActive);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void showDialogApply() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_apply);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        TextView tvEmployer = dialog.findViewById(R.id.tvEmployer);
        TextView tvJob =dialog.findViewById(R.id.tvJob);

        tvJob.setText(binding.txtNameJob.getText());
        tvEmployer.setText(binding.txtNameEmployer.getText());

//        EditText editText = dialog.findViewById(R.id.layout);
        dialog.show();
    }
}