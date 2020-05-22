package cn.lcsw.diningpos.function.keyboard;

class PermissonDeniedException extends Exception {
    public PermissonDeniedException(String no_permisson) {
        super(no_permisson);
    }
}
