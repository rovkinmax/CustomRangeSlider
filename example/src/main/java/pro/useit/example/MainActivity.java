package pro.useit.example;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import com.custom.widget.RangeSeekBar;


public class MainActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView minProgress = (TextView) findViewById(R.id.minProgress);
        final TextView maxProgress = (TextView) findViewById(R.id.maxProgress);
        final RangeSeekBar rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setUnits("F");
        rangeSeekBar.setOnRangeChangeListener(new RangeSeekBar.OnRangeChangeListener() {
            @Override
            public void onRangeChange(final int minValue, final int maxValue)
            {
                minProgress.setText(String.valueOf(minValue));
                maxProgress.setText(String.valueOf(maxValue));
            }
        });
    }
}
