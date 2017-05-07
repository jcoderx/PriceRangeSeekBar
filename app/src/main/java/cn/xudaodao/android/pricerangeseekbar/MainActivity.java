package cn.xudaodao.android.pricerangeseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final int[] RANGE = {0, 100, 200, 300, 400, 500};
    PriceRangeSeekBar rangeSeekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rangeSeekBar = (PriceRangeSeekBar) findViewById(R.id.rangeseekbar);
        rangeSeekBar.setLabels(RANGE, 1, 3);
        rangeSeekBar.setLabelGenerator(new PriceRangeSeekBar.LabelGenerator() {

            @Override
            public String generateLabel(int originalLabel) {
                if (originalLabel == 500) {
                    return "不限";
                } else {
                    return "¥" + originalLabel;
                }
            }
        });


        rangeSeekBar.setOnRangeSelectedListener(new PriceRangeSeekBar.OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(PriceRangeSeekBar PriceRangeSeekBar, int minValue, int maxValue) {
                Toast.makeText(MainActivity.this, "OnRangeSelected:" + minValue + "," + minValue, Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, rangeSeekBar.getMinValue() + "," + rangeSeekBar.getMaxValue(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
