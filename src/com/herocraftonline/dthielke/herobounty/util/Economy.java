package com.herocraftonline.dthielke.herobounty.util;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class Economy {

    private iConomy iconomy;

    public Economy() {}

    public Economy(iConomy iconomy) {
        this.iconomy = iconomy;
    }

    public double getBalance(String name) {
        if (iconomy != null) {
            return iConomy.getBank().getAccount(name).getBalance();
        } else {
            return Double.NaN;
        }
    }

    public double add(String name, double amount) {
        if (iconomy != null) {
            Account acct = iConomy.getBank().getAccount(name);
            if (acct == null) {
                return Double.NaN;
            }
            acct.add(amount);
            return amount;
        } else {
            return Double.NaN;
        }
    }

    public double subtract(String name, double amount, boolean allowNegative) {
        if (iconomy != null) {
            Account acct = iConomy.getBank().getAccount(name);
            if (acct == null) {
                return Double.NaN;
            }
            double balance = acct.getBalance();
            if (balance < amount && !allowNegative) {
                amount = balance;
            }
            acct.subtract(amount);
            return amount;
        } else {
            return Double.NaN;
        }
    }

    public boolean hasAmount(String name, double amount) {
        if (iconomy != null) {
            Account account = iConomy.getBank().getAccount(name);
            if (account == null) {
                return false;
            }
            return account.getBalance() >= amount;
        } else {
            return true;
        }
    }

    public String format(double amount) {
        if (iconomy != null) {
            return iConomy.getBank().format(amount);
        } else {
            return String.valueOf(amount);
        }
    }

    public void setIconomy(iConomy iconomy) {
        this.iconomy = iconomy;
    }
}
