package com.nexustech.comicfire.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.Constants.LIKE_STATUS;
import static com.nexustech.comicfire.utils.HandleActions.intentToProfile;
import static com.nexustech.comicfire.utils.HandleActions.openPopupForOthers;
import static com.nexustech.comicfire.utils.HandleActions.openPopupForOwn;
import static com.nexustech.comicfire.utils.HandleActions.openReportPostPopup;
import static com.nexustech.comicfire.utils.HandleActions.openVPostViewActivity;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<Posts> mPostList;
    Context context;
    String[] menu;
    Activity activity;
    boolean isFollowing;

    public PostAdapter(Context context, Activity activity) {
        this.mPostList = new ArrayList<>();
        this.context = context;
        this.activity = activity;

    }

    public void addAll(List<Posts> newCats) {

        int initSize = newCats.size();
        mPostList.addAll(newCats);
        notifyItemRangeChanged(initSize, newCats.size());

    }

    public String getLastItemId() {
        return mPostList.get(mPostList.size() - 1).getPostId();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_post, parent, false);

        return new PostViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        Handler handler = new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if (!holder.isFriendsPost(mPostList.get(position).getUserId())) {
                    mPostList.remove(position);
                    notifyItemRemoved(position);
                }
            }
        };

        handler.post(runnable);




        holder.tvPostText.setText(mPostList.get(position).getPostText());
        Picasso.get().load(mPostList.get(position).getPostImage()).into(holder.ivPostImage);

        if (mPostList.get(position).getPostImage() == null) {
            holder.ivPostImage.setVisibility(View.GONE);
            int counted = mPostList.get(position).getPostText().length();
            if (counted < 100) {
                holder.tvPostText.setTextSize(35);
                holder.tvPostText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.tvPostText.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
            }

        }
        holder.manageLikeButton(holder.ivLike, mPostList.get(position).getPostId(), context);
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // holder.likeActionHandle(context,mPostList.get(position).getPostId());
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });

        holder.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.tvPostText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToProfile(context, mPostList.get(position).getUserId());
            }
        });
        holder.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToProfile(context, mPostList.get(position).getUserId());
            }
        });
        holder.tvCommentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.tvlikeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVPostViewActivity(context, mPostList.get(position).getPostId());
            }
        });
        holder.setUserDetails(mPostList.get(position).getUserId());

        holder.ivTripleDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_for_menus, null);
                AlertDialog dialog = Utils.configDialog(context, rowView);
                ListView lvMenu = rowView.findViewById(R.id.lv_menu);
                TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
                Utils.setDialogPosition(dialog);
                ArrayAdapter aAdapter;

                if (Utils.isCurrentUser(mPostList.get(position).getUserId())) {
                    menu = context.getResources().getStringArray(R.array.own_post_menu);
                } else {
                    menu = context.getResources().getStringArray(R.array.others_post_menu);
                }


                aAdapter = new ArrayAdapter(context, R.layout.layout_menu_name, R.id.tv_region_name, menu);
                lvMenu.setAdapter(aAdapter);
             /*   Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                    }
                };
                handler.post(runnable);

              */

                lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        dialog.dismiss();
                        TextView textView = view.findViewById(R.id.tv_region_name);
                        String string = textView.getText().toString();
                        if (Utils.isCurrentUser(mPostList.get(position).getUserId())) {

                            openPopupForOwn(holder.ivPostImage, i, mPostList.get(position).getPostId(), mPostList.get(position).getPostText(), context);

                        } else {
                            if (i == 0) {
                                openPopupForOthers(holder.ivPostImage, i, mPostList.get(position).getPostId(), mPostList.get(position).getPostText(), context);
                            } else if (i == 1) {
                                openReportPostPopup(i, mPostList.get(position).getPostId(), mPostList.get(position).getUserId(), context);
                            } else if (i == 2) {
                                openReportPostPopup(i, mPostList.get(position).getPostId(), mPostList.get(position).getUserId(), context);
                            }
                        }
                    }

                });

                tvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();


            }


        });
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvPostText, tvlikeCount, tvCommentsCount,tvChatacterName;
        ImageView ivProfileImage, ivPostImage, ivLike, ivComment, ivTripleDot;
        ConstraintLayout constraintLayout;
        View view;

        DatabaseReference likeRef;


        private FirebaseAuth cfAuth;
        private String currentUserId;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvUserName = itemView.findViewById(R.id.tvProfileName);
            ivProfileImage = itemView.findViewById(R.id.ivProfile);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostText = itemView.findViewById(R.id.tvDescription);
            tvlikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvCommentsCount = itemView.findViewById(R.id.tvCommentCount);
            ivComment = itemView.findViewById(R.id.ivComments);
            ivTripleDot = itemView.findViewById(R.id.trible_dot);
            constraintLayout = itemView.findViewById(R.id.con_layout);
            tvChatacterName=itemView.findViewById(R.id.tv_character_name);
            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();

        }

        public void manageLikeButton(ImageView ivLike, String postKey, Context context) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Views");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvlikeCount.setText(ct + " Views");
                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    } else {
                        tvlikeCount.setText(0 + " Views");
                        //LIKE_STATUS="NOT_LIKED";
                        //ivLike.setImageResource(R.drawable.fire_light_gray);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Comments");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvCommentsCount.setText("Comments " + ct);


                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    } else {
                        tvCommentsCount.setText("Comments " + 0);
                        //LIKE_STATUS="NOT_LIKED";
                        //ivLike.setImageResource(R.drawable.fire_light_gray);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void likeActionHandle(Context context, String postKey) {
            if (LIKE_STATUS.equals("NOT_LIKED")) {
                like(postKey);
            } else {
                unLike(postKey);
            }

        }

        private void unLike(String postKey) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).removeValue();

            LIKE_STATUS = "NOT_LIKED";
            manageLikeButton(ivLike, postKey, context);
        }

        private void like(String postKey) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).child("UserId").setValue(currentUserId);
            LIKE_STATUS = "LIKED";

            manageLikeButton(ivLike, postKey, context);

        }
        public void setUserDetails(String userId){
            DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        String name=dataSnapshot.child("CharacterName").getValue().toString();
                        String displayName=dataSnapshot.child("DisplayName").getValue().toString();
                        String profile=dataSnapshot.child("ProfileImage").getValue().toString();

                        tvChatacterName.setText(name);
                        tvUserName.setText(displayName);

                        Picasso.get().load(profile).into(ivProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public boolean isFriendsPost(String userId) {
            DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE)
                    .child("User").child(Utils.getCurrentUser());
            userRef.child("Followings").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if (dataSnapshot.exists()){
                      if (dataSnapshot.hasChild(userId)||userId.equals(Utils.getCurrentUser())){
                          isFollowing=true;
                      }
                      else {
                          isFollowing=false;
                      }
                  }else {
                      isFollowing=false;
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return isFollowing;
        }
    }
}
