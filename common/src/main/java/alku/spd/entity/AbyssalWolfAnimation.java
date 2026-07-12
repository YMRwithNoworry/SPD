package alku.spd.entity;

final class AbyssalWolfAnimation {
    private AbyssalWolfAnimation() {
    }

    static boolean isMoving(boolean geckoMoving, boolean walkAnimationMoving, float limbSwingAmount,
                            double motionX, double motionZ) {
        return geckoMoving || walkAnimationMoving || Math.abs(limbSwingAmount) > 1.0E-4F
                || motionX * motionX + motionZ * motionZ > 1.0E-5D;
    }
}
