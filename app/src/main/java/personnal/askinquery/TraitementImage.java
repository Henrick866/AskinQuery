package personnal.askinquery;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;

public class TraitementImage {
    public static Bitmap RotateImage(Uri uri, Activity activity){
        try{
            Bitmap imageBase = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        String FullPath = getRealPath(activity, uri);
        ExifInterface exifInterface = new ExifInterface(FullPath);
        Matrix matrix = new Matrix();
        switch(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)){
            case 3 : matrix.postRotate(180);
                break;
            case 6: matrix.postRotate(90);
                break;
            case 8: matrix.postRotate(270);
                break;
        }
        Bitmap imageFinal = Bitmap.createBitmap(imageBase, 0, 0, imageBase.getWidth(), imageBase.getHeight(), matrix, true);
        return imageFinal;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Uri ConvertBitmapToUri(Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null);
        return Uri.parse(path);
    }
    private static String getRealPath(Activity activity, Uri file){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String Path;
        Cursor cursor = activity.getContentResolver().query(file, filePathColumn, null, null, null);
        if(cursor == null){
            return file.getPath();
        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            Path = cursor.getString(idx);
            cursor.close();
            return Path;
        }
    }
    public static Bitmap createSquaredBitmap(Bitmap srcBmp, Context context) {//pour profil
        int dim = Math.max(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 150 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(srcBmp, (px - srcBmp.getWidth()) / 2, (px - srcBmp.getHeight()) / 2, null);

        return dstBmp;
    }
}
