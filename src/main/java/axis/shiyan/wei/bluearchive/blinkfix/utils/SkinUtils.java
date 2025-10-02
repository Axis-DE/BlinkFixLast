package axis.shiyan.wei.bluearchive.blinkfix.utils;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class SkinUtils {
    public static BufferedImage coverImage(BufferedImage skinLayer1, BufferedImage skinLayer2) {

        // Scale Modifier
        int origsize = skinLayer1.getWidth() / 64;

        BufferedImage bi = new BufferedImage(64*origsize, 64*origsize, BufferedImage.TYPE_INT_ARGB);

        //Upper
        BufferedImage upper = new BufferedImage(64*origsize, 32*origsize, BufferedImage.TYPE_INT_ARGB);
        upper.getGraphics().drawImage(skinLayer1, 0, 0, 64*origsize, 32*origsize, 0, 0, 64*origsize, 32*origsize, null);

        bi.getGraphics().drawImage(upper, 0, 0, 64*origsize, 64*origsize, 0, 0, 64*origsize, 64*origsize, null);

        //Arm
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[0], 0, 0, 64*origsize, 64*origsize, -32*origsize, -52*origsize, (-32*origsize) + (64*origsize), (-52*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[1], 0, 0, 64*origsize, 64*origsize, -44*origsize, -52*origsize, (-44*origsize) + (64*origsize), (-52*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[2], 0, 0, 64*origsize, 64*origsize, -36*origsize, -48*origsize, (-36*origsize) + (64*origsize), (-48*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[3], 0, 0, 64*origsize, 64*origsize, -40*origsize, -48*origsize, -40*origsize + 64*origsize, -48*origsize + 64*origsize, null);
        //Leg
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[4], 0, 0, 64*origsize, 64*origsize, -16*origsize, -52*origsize, -16*origsize + 64*origsize, -52*origsize + 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[5], 0, 0, 64*origsize, 64*origsize, -28*origsize, -52*origsize, -28*origsize + 64*origsize, -52*origsize + 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[6], 0, 0, 64*origsize, 64*origsize, -20*origsize, -48*origsize, -20*origsize + 64*origsize, -48*origsize+ 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer1)[7], 0, 0, 64*origsize, 64*origsize, -24*origsize, -48*origsize, -24*origsize + 64*origsize, -48*origsize + 64*origsize, null);


        //Body
        BufferedImage body = new BufferedImage(24*origsize, 16*origsize, BufferedImage.TYPE_INT_ARGB);
        body.getGraphics().drawImage(skinLayer2, 0, 0, 24*origsize, 16*origsize, 16*origsize, 16*origsize, 40*origsize, 32*origsize, null);

        //Arm
        BufferedImage arm = new BufferedImage(16*origsize, 16*origsize, BufferedImage.TYPE_INT_ARGB);
        arm.getGraphics().drawImage(skinLayer2, 0, 0, 16*origsize, 16*origsize, 40*origsize, 16*origsize, 56*origsize, 32*origsize, null);

        //Leg
        BufferedImage leg = new BufferedImage(16*origsize, 16*origsize, BufferedImage.TYPE_INT_ARGB);
        leg.getGraphics().drawImage(skinLayer2, 0, 0, 16*origsize, 16*origsize, 0*origsize, 16*origsize, 16*origsize, 32*origsize, null);

        bi.getGraphics().drawImage(body, 0, 0, 64*origsize, 64*origsize, -16*origsize, -32*origsize, (-16*origsize) + (64*origsize), (-32*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(arm, 0, 0, 64*origsize, 64*origsize, -40*origsize, -32*origsize, (-40*origsize) + (64*origsize), (-32*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(leg, 0, 0, 64*origsize, 64*origsize, 0*origsize, -32*origsize, (0*origsize) + (64*origsize), (-32*origsize) + (64*origsize), null);

        //Arm
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[0], 0, 0, 64*origsize, 64*origsize, -48*origsize, -52*origsize, (-48*origsize) + (64*origsize), (-52*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[1], 0, 0, 64*origsize, 64*origsize, -60*origsize, -52*origsize, (-60*origsize) + (64*origsize), (-52*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[2], 0, 0, 64*origsize, 64*origsize, -52*origsize, -48*origsize, (-52*origsize) + (64*origsize), (-48*origsize) + (64*origsize), null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[3], 0, 0, 64*origsize, 64*origsize, -56*origsize, -48*origsize, -56*origsize + 64*origsize, -48*origsize + 64*origsize, null);

        //Leg
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[4], 0, 0, 64*origsize, 64*origsize, -0*origsize, -52*origsize, -0*origsize + 64*origsize, -52*origsize + 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[5], 0, 0, 64*origsize, 64*origsize, -12*origsize, -52*origsize, -12*origsize + 64*origsize, -52*origsize + 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[6], 0, 0, 64*origsize, 64*origsize, -4*origsize, -48*origsize, -4*origsize + 64*origsize, -48*origsize+ 64*origsize, null);
        bi.getGraphics().drawImage(CalcSkin(skinLayer2)[7], 0, 0, 64*origsize, 64*origsize, -8*origsize, -48*origsize, -8*origsize + 64*origsize, -48*origsize + 64*origsize, null);

        return bi;
    }

    //CUT THE IMAGE UP FOR REARANGEMENT
    public static BufferedImage[] CalcSkin (BufferedImage skinIn){
        //Scale Modifier
        int origsize = skinIn.getWidth()/64;

        //Arm Front
        BufferedImage armF = new BufferedImage(12*origsize, 12*origsize, BufferedImage.TYPE_INT_ARGB);
        armF.getGraphics().drawImage(skinIn, 0, 0, 12*origsize, 12*origsize, 40*origsize, 20*origsize, (40*origsize) + (12*origsize), (20*origsize) + (12*origsize), null);
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-armF.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        armF = op.filter(armF, null);

        //Arm Back
        BufferedImage armB = new BufferedImage(4*origsize, 12*origsize, BufferedImage.TYPE_INT_ARGB);
        armB.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 12*origsize, 52*origsize, 20*origsize, (52*origsize) + (4*origsize), (20*origsize) + (12*origsize), null);
        AffineTransform txab = AffineTransform.getScaleInstance(-1, 1);
        txab.translate(-armB.getWidth(null), 0);
        AffineTransformOp opab = new AffineTransformOp(txab, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        armB = opab.filter(armB, null);

        //Arm Top
        BufferedImage armT = new BufferedImage(4*origsize, 4*origsize, BufferedImage.TYPE_INT_ARGB);
        armT.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 4*origsize, 44*origsize, 16*origsize, (44*origsize) + (4*origsize), (16*origsize) + (4*origsize), null);
        AffineTransform txat = AffineTransform.getScaleInstance(-1, 1);
        txat.translate(-armT.getWidth(null), 0);
        AffineTransformOp opat = new AffineTransformOp(txat, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        armT = opat.filter(armT, null);

        //Arm Bottom
        BufferedImage armBo = new BufferedImage(4*origsize, 4*origsize, BufferedImage.TYPE_INT_ARGB);
        armBo.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 4*origsize, 48*origsize, 16*origsize, (48*origsize) + (4*origsize), (16*origsize) + (4*origsize), null);
        armBo = opat.filter(armBo, null);

        //Leg Front
        BufferedImage legF = new BufferedImage(12*origsize, 12*origsize, BufferedImage.TYPE_INT_ARGB);
        legF.getGraphics().drawImage(skinIn, 0, 0, 12*origsize, 12*origsize, 0, 20*origsize, 0+12*origsize, (20*origsize) + (12*origsize), null);
        legF = op.filter(legF, null);

        //Leg Back
        BufferedImage legB = new BufferedImage(4*origsize, 12*origsize, BufferedImage.TYPE_INT_ARGB);
        legB.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 12*origsize, 12*origsize, 20*origsize, (12*origsize) + (4*origsize), (20*origsize) + (12*origsize), null);
        legB = opab.filter(legB, null);

        //Leg Top
        BufferedImage legT = new BufferedImage(4*origsize, 4*origsize, BufferedImage.TYPE_INT_ARGB);
        legT.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 4*origsize, 4*origsize, 16*origsize, (4*origsize) + (4*origsize), (16*origsize) + (4*origsize), null);
        legT = opat.filter(legT, null);

        //Leg Bottom
        BufferedImage legBo = new BufferedImage(4*origsize, 4*origsize, BufferedImage.TYPE_INT_ARGB);
        legBo.getGraphics().drawImage(skinIn, 0, 0, 4*origsize, 4*origsize, 8*origsize, 16*origsize, (8*origsize) + (4*origsize), (16*origsize) + (4*origsize), null);
        legBo = opab.filter(legBo, null);


        BufferedImage[] returnimages = new BufferedImage[8];

        returnimages[0] = armF;
        returnimages[1] = armB;
        returnimages[2] = armT;
        returnimages[3] = armBo;

        returnimages[4] = legF;
        returnimages[5] = legB;
        returnimages[6] = legT;
        returnimages[7] = legBo;

        return returnimages;

    }
}