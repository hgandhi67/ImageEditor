package com.app.image.editor.javaclasses.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.image.editor.R;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.rm.freedrawview.FreeDrawView;
import com.xw.repo.BubbleSeekBar;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;

import jp.wasabeef.blurry.Blurry;

public class ImageEditingActivity extends AppCompatActivity implements View.OnClickListener {

    //Data
    private String imagePath = "";

    //Components
    private RelativeLayout editLayout, mainImageLayout;
    private FrameLayout rootEditLayout;
    private FloatingActionButton editButton;
    private ImageView editingImage;
    private FreeDrawView freeDrawView;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView enhanceOption, drawOption, textOption, writtenText;

    //Flags
    private boolean editOpenFlag = false;

    //Enhance
    private ImageView crop, autoContrast, invertColors;
    private static final float[] NEGATIVE = {-1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };
    private boolean cropFlag = false;
    private boolean firstTime = true;

    //Draw on Image
    private TextView undo, redo;
    private BubbleSeekBar thickness, alpha;
    private AppCompatButton colorButton;

    //Text On Image
    private AppCompatEditText editText;
    //    private Spinner fontSpinner;
    private BubbleSeekBar sizeSeekBar;
    private AppCompatButton textColorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editing);

        if (getIntent().getExtras() != null) {
            imagePath = getIntent().getExtras().getString("image_path");
        }
        InitViews();
        Listeners();
    }

    public void InitViews() {
        mainImageLayout = findViewById(R.id.main_image_layout);
        editButton = findViewById(R.id.edit_button);
        editLayout = findViewById(R.id.edit_bottom_sheet);
        editingImage = findViewById(R.id.editing_image);
        rootEditLayout = findViewById(R.id.edit_room);
        enhanceOption = findViewById(R.id.enhance_image);
        drawOption = findViewById(R.id.draw_on_image);
        textOption = findViewById(R.id.add_text_on_image);
        freeDrawView = findViewById(R.id.free_draw_view);
        writtenText = findViewById(R.id.text_written);
        bottomSheetBehavior = BottomSheetBehavior.from(editLayout);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    editOpenFlag = true;
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    editOpenFlag = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        if (!imagePath.equals("")) {
            editingImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } else {
            Toast.makeText(ImageEditingActivity.this, "The image not created!", Toast.LENGTH_SHORT).show();
        }
        EnhanceLayoutAddition();
    }

    public void Listeners() {
        editButton.setOnClickListener(this);
        enhanceOption.setOnClickListener(this);
        drawOption.setOnClickListener(this);
        textOption.setOnClickListener(this);
    }

    public void EnhanceLayoutAddition() {
        if (!firstTime) {
            saveImageViewImage();
            firstTime = false;
        }
//        saveImageViewImage();
        rootEditLayout.removeAllViews();
        freeDrawView.setVisibility(View.GONE);
        writtenText.setVisibility(View.GONE);
        SetBackgroundSelected(enhanceOption);
        SetBackgroundUnselected(drawOption, textOption);
        View enhanceView = LayoutInflater.from(ImageEditingActivity.this).inflate(R.layout.enhance_image_layout, null);
        crop = enhanceView.findViewById(R.id.crop);
//        rotate = enhanceView.findViewById(R.id.rotate);
        autoContrast = enhanceView.findViewById(R.id.auto_blur);
        invertColors = enhanceView.findViewById(R.id.invert_color);

        crop.setOnClickListener(this);
//        rotate.setOnClickListener(this);
        autoContrast.setOnClickListener(this);
        invertColors.setOnClickListener(this);

        rootEditLayout.addView(enhanceView);
    }

    public void DrawOnImageLayoutAddition() {
        saveImageViewImage();
        rootEditLayout.removeAllViews();
        freeDrawView.setVisibility(View.VISIBLE);
        writtenText.setVisibility(View.GONE);
        SetBackgroundSelected(drawOption);
        SetBackgroundUnselected(enhanceOption, textOption);
        View drawOnImageView = LayoutInflater.from(ImageEditingActivity.this).inflate(R.layout.draw_on_image_layout, null);
        undo = drawOnImageView.findViewById(R.id.undo_draw);
        redo = drawOnImageView.findViewById(R.id.redo_draw);
        thickness = drawOnImageView.findViewById(R.id.brush_size_seek_bar);
        alpha = drawOnImageView.findViewById(R.id.brush_alpha_seek_bar);
        colorButton = drawOnImageView.findViewById(R.id.color_picker_button);
        undo.setOnClickListener(this);
        redo.setOnClickListener(this);
        colorButton.setOnClickListener(this);
        thickness.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                freeDrawView.setPaintWidthDp(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
        alpha.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                freeDrawView.setPaintAlpha(progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        rootEditLayout.addView(drawOnImageView);
    }

    public void TextOnImageLayoutAddition() {
        saveImageViewImage();
        rootEditLayout.removeAllViews();
        freeDrawView.setVisibility(View.GONE);
        writtenText.setVisibility(View.VISIBLE);
        SetBackgroundSelected(textOption);
        SetBackgroundUnselected(enhanceOption, drawOption);
        View textOnImageView = LayoutInflater.from(ImageEditingActivity.this).inflate(R.layout.text_on_image_layout, null);
        editText = textOnImageView.findViewById(R.id.text_edit_text);
//        fontSpinner = textOnImageView.findViewById(R.id.font_spinner);
        sizeSeekBar = textOnImageView.findViewById(R.id.text_size_seek_bar);
        textColorButton = textOnImageView.findViewById(R.id.text_color_picker_button);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                writtenText.setText(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        sizeSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                writtenText.setTextSize(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
        textColorButton.setOnClickListener(this);

        rootEditLayout.addView(textOnImageView);
    }

    public void SetBackgroundSelected(TextView textView) {
        textView.setBackgroundColor(getResources().getColor(R.color.white));
        textView.setTextColor(getResources().getColor(R.color.bg_color));
    }

    public void SetBackgroundUnselected(TextView textView, TextView textView2) {
        textView.setBackgroundColor(getResources().getColor(R.color.bg_color));
        textView.setTextColor(getResources().getColor(R.color.white));

        textView2.setBackgroundColor(getResources().getColor(R.color.bg_color));
        textView2.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.edit_button:
                if (!editOpenFlag) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    editOpenFlag = true;
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    editOpenFlag = false;
                }
                break;
            case R.id.enhance_image:
                EnhanceLayoutAddition();
                break;
            case R.id.draw_on_image:
                DrawOnImageLayoutAddition();
                break;
            case R.id.add_text_on_image:
                TextOnImageLayoutAddition();
                break;
            case R.id.crop:
                cropFlag = true;
                UCrop.of(Uri.fromFile(new File(imagePath)), Uri.fromFile(new File(getCacheDir(), "tempCropImage.jpg")))
                        .withMaxResultSize(editingImage.getMaxWidth(), editingImage.getMaxHeight())
                        .start(ImageEditingActivity.this);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveImageViewImage();
                    }
                }, 1000);
                break;
//            case R.id.rotate:
//                if (currentRotation != 360.0f) {
//                    currentRotation += 90.0f;
//                } else {
//                    currentRotation = 90.0f;
//                }
//                editingImage.setRotation(currentRotation);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        saveImageViewImage();
//                    }
//                }, 1000);
//                break;
            case R.id.auto_blur:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(ImageEditingActivity.this);
                builder2.setTitle("Blur Image?")
                        .setMessage("Are you sure you want to blur image? \n Note: This cannot be undone.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Blurry.with(ImageEditingActivity.this).capture(editingImage).into(editingImage);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        saveImageViewImage();
                                    }
                                }, 1000);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.invert_color:
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageEditingActivity.this);
                builder.setTitle("Invert Colors?")
                        .setMessage("Are you sure you want to invert colors? \n Note: This cannot be undone.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                editingImage.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        saveImageViewImage();
                                    }
                                }, 1000);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.undo_draw:
                freeDrawView.undoLast();
                break;
            case R.id.redo_draw:
                freeDrawView.redoLast();
                break;
            case R.id.color_picker_button:
                ColorPickerDialogBuilder
                        .with(ImageEditingActivity.this)
                        .setTitle("Choose color")
                        .initialColor(R.color.white)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                freeDrawView.setPaintColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
                break;
            case R.id.text_color_picker_button:
                ColorPickerDialogBuilder
                        .with(ImageEditingActivity.this)
                        .setTitle("Choose color")
                        .initialColor(R.color.white)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                writtenText.setTextColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    editingImage.setImageURI(resultUri);
                }
            }
        }
    }

    public void saveImageViewImage() {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
            mainImageLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            mainImageLayout.buildDrawingCache();
            Bitmap b1 = mainImageLayout.getDrawingCache();
            Bitmap bitmap = b1.copy(Bitmap.Config.ARGB_8888, true);
            mainImageLayout.destroyDrawingCache();
            FileOutputStream outStream = null;
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }
            Log.e("IMAGE-PATH", "The path is: " + imageFile.getAbsolutePath());
            outStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            MediaScannerConnection.scanFile(ImageEditingActivity.this, new String[]{imagePath},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Bitmap newBitmap = BitmapFactory.decodeFile(path);
                            editingImage.setImageBitmap(newBitmap);
                        }
                    });
        } catch (Exception e) {
            Log.e("ERROR", "The error saving image is: " + e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_menu_item) {
            saveImageViewImage();
            Toast.makeText(ImageEditingActivity.this, "Your image has been saved.", Toast.LENGTH_SHORT).show();
            finish();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cropFlag) {
            cropFlag = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    saveImageViewImage();
                }
            }, 1000);
        }
    }
}
/*
*     public void LoadAnimation() {
        slideUpAnimation = AnimationUtils.loadAnimation(ImageEditingActivity.this, R.anim.slide_up);
        slideDownAnimation = AnimationUtils.loadAnimation(ImageEditingActivity.this, R.anim.slide_down);
    }
* */
