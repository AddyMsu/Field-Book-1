package com.fieldbook.tracker.traits;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fieldbook.tracker.R;
import com.fieldbook.tracker.activities.CollectActivity;
import com.fieldbook.tracker.database.models.ObservationModel;
import com.fieldbook.tracker.dialogs.DatePickerFragment;
import com.fieldbook.tracker.preferences.GeneralKeys;
import com.fieldbook.tracker.preferences.PreferenceKeys;
import com.fieldbook.tracker.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTraitLayout extends BaseTraitLayout {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    FloatingActionButton addDayBtn;
    FloatingActionButton minusDayBtn;
    FloatingActionButton saveDayBtn;
    private TextView datePreviewText;
    private String date;
    private boolean isBlocked = true; //tracks when multi measures can be navigated
    private boolean isFirstLoad = true;

    public DateTraitLayout(Context context) {
        super(context);
    }

    public DateTraitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateTraitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setNaTraitsText() {
        getCollectInputView().setText("NA");
    }

    @Override
    public String type() {
        return "date";
    }

    @Override
    public int layoutId() {
        return R.layout.trait_date;
    }

    @Override
    public boolean block() {
        return isBlocked;
    }

    //this will display text color as the preference color
    private void forceDataSavedColor() {
        getCollectInputView().setTextColor(Color.parseColor(getDisplayColor()));
    }

    @Override
    public void init(Activity act) {

        Calendar calendar = Calendar.getInstance();

        date = getPrefs().getString(GeneralKeys.CALENDAR_LAST_SAVED_DATE, dateFormat.format(calendar.getTime()));
        log();

        addDayBtn = act.findViewById(R.id.addDateBtn);
        minusDayBtn = act.findViewById(R.id.minusDateBtn);
        saveDayBtn = act.findViewById(R.id.enterBtn);
        datePreviewText = act.findViewById(R.id.datePreviewText);

        updatePreviewDate(calendar);

        ImageButton calendarVisibilityBtn = act.findViewById(R.id.trait_date_calendar_visibility_btn);

        String minusDayTts = getContext().getString(R.string.trait_date_minus_day_tts);
        String openCalendarTts = getContext().getString(R.string.trait_date_open_calendar_tts);
        String nextDayTts = getContext().getString(R.string.trait_date_next_day_tts);

        /*
         * When the calendar view visibility button is pressed it starts the date picker dialog.
         */
        calendarVisibilityBtn.setOnClickListener((View) -> {

            DialogFragment newFragment = new DatePickerFragment(getContext()).newInstance(dateFormat, (y, m, d) -> {

                calendar.set(y, m, d);

                updatePreviewDate(calendar);

                updateViewDate(calendar);

                String rep = ((CollectActivity) getContext()).getRep();

                //save date to db
                updateObservation(getCurrentTrait(), dateFormat.format(calendar.getTime()));

                triggerTts(getTtsFromCalendar(calendar));

                isBlocked = false;

                return true;
            });

            triggerTts(openCalendarTts);
            newFragment.show(((CollectActivity) getContext()).getSupportFragmentManager(),
                    "datePicker");
        });

        // Add day
        addDayBtn.setOnClickListener(arg0 -> {

            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(nextDayTts);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Add day
            calendar.add(Calendar.DATE, 1);

            updatePreviewDate(calendar);

            isBlocked = false;
        });

        // Minus day
        minusDayBtn.setOnClickListener(arg0 -> {

            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(minusDayTts);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Subtract day, rewrite date
            calendar.add(Calendar.DATE, -1);

            updatePreviewDate(calendar);

            isBlocked = false;
        });

        // Saving date data
        saveDayBtn.setOnClickListener(arg0 -> {

            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(getTtsFromCalendar(calendar));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String previewText = datePreviewText.getText().toString();
            getCollectInputView().setText(previewText);

            if (getPrefs().getBoolean(PreferenceKeys.USE_DAY_OF_YEAR, false)) {
                updateObservation(getCurrentTrait(), String.valueOf(calendar.get(Calendar.DAY_OF_YEAR)));
            } else {
                updateObservation(getCurrentTrait(), dateFormat.format(calendar.getTime()));
            }

            isBlocked = false;
        });

        saveDayBtn.requestFocus();
    }

    private String getTtsFromCalendar(Calendar calendar) {

        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return month + " " + day;
    }

    private void updatePreviewDate(Calendar calendar) {
        date = dateFormat.format(calendar.getTime());
        log();
        setDatePreviewText(getMonthForInt(calendar.get(Calendar.MONTH)),
                String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.DAY_OF_MONTH)));
    }

    private void updateViewDate(Calendar calendar) {

        date = dateFormat.format(calendar.getTime());
        log();

        String dayOfMonth = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String monthText = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        String yearText = Integer.toString(calendar.get(Calendar.YEAR));

        getPrefs().edit()
                .putString(GeneralKeys.CALENDAR_LAST_SAVED_DATE, yearText + "-" + monthText + "-" + dayOfMonth)
                .apply();

        setDateText(getMonthForInt(calendar.get(Calendar.MONTH)),
                String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.DAY_OF_MONTH)));
    }

    private void log() {
        Log.d("DATE", date);
    }

    private void loadSelectedDate() {
        try {
            ObservationModel model = getCurrentObservation();
            date = model.getValue();
            log();
            //afterLoadExists((CollectActivity) getContext(), date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDateAndView() {

        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(dateFormat.parse(date));

            //set month/day text and color
            setDateText(getMonthForInt(calendar.get(Calendar.MONTH)), String.format(Locale.getDefault(),
                    "%02d", calendar.get(Calendar.DAY_OF_MONTH)));

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadLayout() {
        super.loadLayout();
        isFirstLoad = true;
    }

    @Override
    public void refreshLayout(Boolean onNew) {
        if (!block() || isFirstLoad) {
            isFirstLoad = false;
            super.refreshLayout(onNew);
            if (!onNew) {
                ObservationModel model = getCurrentObservation();
                if (model != null) {
                    date = getCurrentObservation().getValue();
                }
                refreshDateText(date);
            }
        } else {
            Utils.makeToast(getContext(), getContext().getString(R.string.view_repeated_values_add_button_fail));
        }
    }

    private void refreshDateText(String value) {

        //first check if observation values is observed for this plot and the value is not NA
        if (value != null && !value.equals("NA")) {

            forceDataSavedColor();

            //there is a FB preference to save dates as Day of year between 1-365
            if (value.length() < 4 && value.length() > 0) {
                Calendar calendar = Calendar.getInstance();

                //convert day of year to yyyy-mm-dd string
                date = value;
                log();

                calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(date));
                date = dateFormat.format(calendar.getTime());
                log();

                setDateText(getMonthForInt(calendar.get(Calendar.MONTH)), String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH)));

            } else if (value.contains(".")) {
                //convert from yyyy.mm.dd to yyyy-mm-dd
                String[] oldDate = value.split("\\.");
                date = oldDate[0] + "-" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(oldDate[1])) + "-" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(oldDate[2]));
                log();

                //set month/day text and color
                setDateText(getMonthForInt(Integer.parseInt(oldDate[1]) - 1), oldDate[2]);

            } else {

                //new format
                if (!value.isEmpty()) {
                    try {
                        date = dateFormat.parse(value).toString();
                        log();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                parseDateAndView();
            }

        } else if (value != null) {

            //NA is saved as the date
            getCollectInputView().setText("NA");

            forceDataSavedColor();
        }
    }

    @Override
    public void afterLoadExists(CollectActivity act, @Nullable String value) {
        super.afterLoadExists(act, value);

        refreshDateText(value);

        isBlocked = false;
    }

    @Override
    public void afterLoadNotExists(CollectActivity act) {
        super.afterLoadNotExists(act);

        //if data does not exist, use the current date as a default value
        final Calendar c = Calendar.getInstance();

        updatePreviewDate(c);

        isBlocked = true;
    }

    @Override
    public void deleteTraitListener() {
        removeTrait(getCurrentTrait());

        super.deleteTraitListener();

        if (getCurrentObservation() != null) {

            loadSelectedDate();

            parseDateAndView();

        } else { // clear the editText
            getCollectInputView().setText("");
        }
    }

    /**
     * Get month name based on numeric value
     */
    public String getMonthForInt(int m) {
        String month = "invalid";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();

        if (m >= 0 && m <= 11) {
            month = months[m];
        }

        return month;
    }

    private void setDatePreviewText(String month, String day) {
        datePreviewText.setText(month + " " + day);
    }

    private void setDateText(String month, String day) {
        getCollectInputView().setText(month + " " + day);
    }

    @Override
    public String decodeValue(String value) {
        Calendar c = Calendar.getInstance();
        try {
            Date d = dateFormat.parse(value);
            if (d != null) {
                c.setTime(d);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getMonthForInt(c.get(Calendar.MONTH)) + " " + String.format(Locale.getDefault(), "%02d", c.get(Calendar.DAY_OF_MONTH));
    }

    @NonNull
    @Override
    public Boolean validate(String data) {
        boolean useDayOfYear = getPrefs().getBoolean(PreferenceKeys.USE_DAY_OF_YEAR, false);
        try {
            if (useDayOfYear) {
                Integer.parseInt(data);
            } else {
                Date d = dateFormat.parse(data);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}