package com.strangenaut.the_game_of_life.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.os.Bundle;

import com.strangenaut.the_game_of_life.R;
import com.strangenaut.the_game_of_life.databinding.ActivityMainBinding;
import com.strangenaut.the_game_of_life.domain.model.Field;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private int lightCoral, lightRed, darkGray, gray, white, darkenedWhite;
    private Drawable playIcon, pauseIcon;

    private Field matrix;
    private int firstTouchDistance = -1;
    private int cellSize, tempCellSize;
    private int screenWidth, screenHeight;
    private int timelapseStepDelay = 50;
    private float dp;
    private Point currentCell;

    private Bitmap bitmap;
    private Canvas canvas;
    private TimelapseThread timelapse;

    private static boolean isEraserEnabled = false;
    private static boolean isTorus = false;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        playIcon = ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play, null);
        pauseIcon = ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null);

        lightCoral = ResourcesCompat.getColor(getResources(), R.color.lightCoral, null);
        lightRed = ResourcesCompat.getColor(getResources(), R.color.lightRed, null);
        darkGray = ResourcesCompat.getColor(getResources(), R.color.darkGray, null);
        gray = ResourcesCompat.getColor(getResources(), R.color.gray, null);
        white = ResourcesCompat.getColor(getResources(), R.color.white, null);;
        darkenedWhite = ResourcesCompat.getColor(getResources(), R.color.white_1, null);

        setContentView(binding.getRoot());
        binding.ivField.setOnTouchListener(this);
        binding.clMain.setOnTouchListener(this);

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        currentCell = new Point();
        screenWidth = screenSize.x;
        screenHeight = screenSize.y;

        dp = getResources().getDisplayMetrics().density;
        cellSize = (int)(20 * dp);
        tempCellSize = cellSize;

        binding.sbFieldWidth.setMax(screenWidth / cellSize);
        binding.sbFieldWidth.setProgress(10);
        binding.etFieldWidth.setText(String.valueOf(binding.sbFieldWidth.getProgress()));

        binding.sbFieldHeight.setMax(screenHeight / cellSize);
        binding.sbFieldHeight.setProgress(15);
        binding.etFieldHeight.setText(String.valueOf(binding.sbFieldHeight.getProgress()));

        binding.sbTimelapseSpeed.setProgress(20);
        binding.etTimelapseSpeed.setText(String.valueOf(binding.sbTimelapseSpeed.getProgress()));

        createField(
                cellSize,
                binding.sbFieldWidth.getProgress(),
                binding.sbFieldHeight.getProgress()
        );

        matrix = new Field(
                binding.sbFieldWidth.getProgress(),
                binding.sbFieldHeight.getProgress()
        );
        timelapse = new TimelapseThread("timelapse");

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isNewSizeSuitable(cellSize)) {
                    createField(
                            cellSize,
                            binding.sbFieldWidth.getProgress(),
                            binding.sbFieldHeight.getProgress());
                }
                if (seekBar.getId() == R.id.sbFieldWidth) {
                    binding.etFieldWidth.setText(String.valueOf(seekBar.getProgress()));
                } else {
                    binding.etFieldHeight.setText(String.valueOf(seekBar.getProgress()));
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };

        binding.sbFieldWidth.setOnSeekBarChangeListener(seekBarListener);
        binding.sbFieldHeight.setOnSeekBarChangeListener(seekBarListener);

        binding.sbTimelapseSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timelapseStepDelay = (int)(1000.0 / seekBar.getProgress());
                binding.etTimelapseSpeed.setText(String.valueOf(seekBar.getProgress()));
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        binding.etTimelapseSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setSeekBarValueFromEditText(
                        s.toString(),
                        binding.sbTimelapseSpeed,
                        binding.etTimelapseSpeed
                );
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });

        binding.etFieldWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setSeekBarValueFromEditText(
                        s.toString(),
                        binding.sbFieldWidth,
                        binding.etFieldWidth
                );
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });

        binding.etFieldHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setSeekBarValueFromEditText(
                        s.toString(),
                        binding.sbFieldHeight,
                        binding.etFieldHeight
                );
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });
    }

    private void setSeekBarValueFromEditText (String text, SeekBar seekBar, EditText textNumber) {
        int value = seekBar.getProgress();
        int length;

        if (text.length() > 0) {
            value = Integer.parseInt(text);
        }
        if (value < 1) {
            value = 1;
        }
        if (value > seekBar.getMax()) {
            value = seekBar.getMax();
        }
        if (value < 1 || value > seekBar.getMax()) {
            textNumber.setText(String.valueOf(value));
        }
        length = textNumber.getText().length();
        textNumber.setSelection(length);
        seekBar.setProgress(value);
    }

    private boolean isNewSizeSuitable(int cellSize) {
        return binding.sbFieldWidth.getProgress() * cellSize <= screenWidth &&
                binding.sbFieldHeight.getProgress() * cellSize <= screenHeight;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (timelapse.isAlive()) {
            return false;
        }

        int pointerCount = motionEvent.getPointerCount();

        switch (pointerCount) {
            case 1:
                binding.tlSettings.setVisibility(View.INVISIBLE);
                if (view.getId() != R.id.ivField) {
                    return false;
                }

                Point fingerLocation = new Point(
                        (int)motionEvent.getX(0),
                        (int)motionEvent.getY(0)
                );
                Point newCell = getCurrentCell(fingerLocation);
                if (!newCell.equals(currentCell)) {
                    currentCell = newCell;
                    if (newCell.x >= 0 && newCell.y >= 0 &&
                            newCell.x  < matrix.getWidth() && newCell.y < matrix.getHeight()
                    ) {
                        matrix.setCell(newCell.x, newCell.y, !isEraserEnabled);
                    }

                    if (isEraserEnabled) {
                        drawCell(newCell, cellSize, white);
                    } else {
                        drawCell(newCell, cellSize, lightCoral);
                    }
                    binding.ivField.setImageBitmap(bitmap);
                }
                break;
            case 2:
                Point point1 = new Point(
                        (int) motionEvent.getX(0),
                        (int) motionEvent.getY(0)
                );
                Point point2 = new Point(
                        (int) motionEvent.getX(1),
                        (int) motionEvent.getY(1)
                );
                int distance = (int) Math.hypot(point2.x - point1.x, point2.y - point1.y);
                if (firstTouchDistance < 0) {
                    firstTouchDistance = distance;
                }

                float coefficient = (float)distance / firstTouchDistance;
                int newCellSize = (int)(cellSize * coefficient);
                if (isNewSizeSuitable(newCellSize) && newCellSize >= 15 * dp) {
                    binding.sbFieldWidth.setMax(screenWidth / newCellSize);
                    binding.sbFieldHeight.setMax(screenHeight / newCellSize);
                    createField(
                            newCellSize,
                            binding.sbFieldWidth.getProgress(),
                            binding.sbFieldHeight.getProgress());
                    tempCellSize = newCellSize;
                    drawMatrix(newCellSize);
                }
                break;
        }
        if (pointerCount != 2) {
            cellSize = tempCellSize;
            firstTouchDistance = -1;
        }
        return true;
    }

    private void createField(int cellSize, int fieldWidth, int fieldHeight) {
        bitmap = Bitmap.createBitmap(
                fieldWidth * cellSize,
                fieldHeight * cellSize,
                Bitmap.Config.ARGB_8888);
        Paint pen = new Paint();
        pen.setColor(darkGray);
        pen.setAntiAlias(true);
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(cellSize / 50f);

        canvas = new Canvas(bitmap);
        for (int i = 0; i <= canvas.getWidth(); i += cellSize) {
            canvas.drawLine(i, 0, i, canvas.getHeight(), pen);
        }
        for (int i = 0; i <= canvas.getHeight(); i += cellSize) {
            canvas.drawLine(0, i, canvas.getWidth(), i, pen);
        }

        if (matrix == null ||
                fieldWidth != matrix.getWidth() ||
                fieldHeight != matrix.getHeight()
        ) {
            matrix = new Field(fieldWidth, fieldHeight);
        }

        drawMatrix(cellSize);

        binding.ivField.setImageBitmap(bitmap);
    }

    private void drawCell(Point cell, int cellSize, int cellColor) {
        Paint pen = new Paint();
        pen.setColor(cellColor);
        pen.setAntiAlias(true);
        Point leftTop = new Point(cell.x * cellSize + 1, cell.y * cellSize + 1);
        canvas.drawRect(leftTop.x, leftTop.y, leftTop.x + cellSize - 2, leftTop.y + cellSize - 2, pen);
    }

    private void drawMatrix(int cellSize) {
        for (int i = 0; i < matrix.getWidth(); i++) {
            for (int j = 0; j < matrix.getHeight(); j++) {
                if (matrix.getCellPreviousState(i, j) != matrix.getCell(i, j)
                        || timelapse == null
                        || !timelapse.isAlive()) {
                    drawCell(new Point(i, j), cellSize, matrix.getCell(i, j) ? lightCoral : white);
                    binding.ivField.setImageBitmap(bitmap);
                }
            }
        }
    }

    private Point getCurrentCell (Point fingerLocation) {
        return new Point(fingerLocation.x / cellSize, fingerLocation.y / cellSize);
    }


    // Tools events
    public void onClickTimeLapse(View view) {
        if (timelapse != null && timelapse.isAlive()) {
            timelapse.interrupt();
            setEnabledSeekBarsAndEditTexts(true);
            setEnabledButtons(true);
            view.setForeground(playIcon);
        } else {
            setEnabledSeekBarsAndEditTexts(false);
            setEnabledButtons(false);
            view.setForeground(pauseIcon);
            timelapse = new TimelapseThread("timelapse");
            timelapse.start();
        }
    }

    public void onClickNewGeneration(View view) {
        matrix.updateGeneration(isTorus);
        drawMatrix(cellSize);
    }

    public void onClickEraser(View view) {
        isEraserEnabled = !isEraserEnabled;
        binding.btnErase.setBackgroundColor(isEraserEnabled ? lightCoral : lightRed);
    }

    public void onClickClearField(View view) {
        if (timelapse != null && timelapse.isAlive()) {
            timelapse.interrupt();
            setEnabledSeekBarsAndEditTexts(true);
            setEnabledButtons(true);
            binding.btnTimelapse.setForeground(playIcon);
        }
        matrix.clearField();
        drawMatrix(cellSize);
    }

    private void setEnabledSeekBarsAndEditTexts(boolean isEnabled) {
        binding.etFieldWidth.setEnabled(isEnabled);
        binding.etFieldHeight.setEnabled(isEnabled);
        binding.sbFieldWidth.setEnabled(isEnabled);
        binding.sbFieldHeight.setEnabled(isEnabled);
    }

    private void setEnabledButtons(boolean isEnabled) {
        binding.btnUpdateGeneration.setEnabled(isEnabled);
        binding.btnErase.setEnabled(isEnabled);
        if (isEnabled) {
            binding.btnUpdateGeneration.setBackgroundColor(lightCoral);
            binding.btnErase.setBackgroundColor(isEraserEnabled ? lightCoral : lightRed);
        } else {
            binding.btnUpdateGeneration.setBackgroundColor(gray);
            binding.btnErase.setBackgroundColor(gray);
        }
    }

    public void toggleLayoutVisibility(View layout) {
        boolean isVisible = layout.getVisibility() == View.VISIBLE;
        layout.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
    }

    public void onClickToggleTools(View view) {
        toggleLayoutVisibility(binding.llTools);
    }

    public void onClickToggleSettings(View view) {
        toggleLayoutVisibility(binding.tlSettings);
    }

    public void onClickIsTorus(View view) {
        Switch flatTorusSwitch = (Switch) view;
        Drawable trackDrawable = flatTorusSwitch.getTrackDrawable();
        Drawable thumbDrawable = flatTorusSwitch.getThumbDrawable();

        if (flatTorusSwitch.isChecked()) {
            trackDrawable.setTint(lightCoral);
            thumbDrawable.setTint(lightCoral);
        } else {
            trackDrawable.setTint(darkGray);
            thumbDrawable.setTint(darkenedWhite);
        }
        isTorus = flatTorusSwitch.isChecked();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timelapse.canTimelapseRun = false;
        timelapse.interrupt();
    }

    class TimelapseThread extends Thread implements Runnable {

        private boolean canTimelapseRun = true;

        public TimelapseThread(String name) {
            super(name);
        }

        public void run() {
            try {
                while (canTimelapseRun) {
                    matrix.updateGeneration(isTorus);
                    drawMatrix(cellSize);
                    Thread.sleep(timelapseStepDelay);
                }
            } catch (InterruptedException ex) {
                canTimelapseRun = false;
            }
        }
    }
}