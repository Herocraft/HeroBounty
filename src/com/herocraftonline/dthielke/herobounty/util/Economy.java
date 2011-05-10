package com.herocraftonline.dthielke.herobounty.util;

import com.iConomy.iConomy;
import com.iConomy.system.Account;

public class Economy {

    private iConomy iconomy;

    public Economy() {}

    public Economy(iConomy iconomy) {
        this.iconomy = iconomy;
    }

    public double getBalance(String name) {
        if (iconomy != null) {
            return iConomy.getAccount(name).getHoldings().balance();
        } else {
            return Double.NaN;
        }
    }

    public double add(String name, double amount) {
        if (iconomy != null) {
            Account acct = iConomy.getAccount(name);
            if (acct == null) {
                return Double.NaN;
            }
            acct.getHoldings().add(amount);
            return amount;
        } else {
            return Double.NaN;
        }
    }

    public double subtract(String name, double amount, boolean allowNegative) {
        if (iconomy != null) {
            Account acct = iConomy.getAccount(name);
            if (acct == null) {
                return Double.NaN;
            }
            double balance = acct.getHoldings().balance();
            if (balance < amount && !allowNegative) {
                amount = balance;
            }
            acct.getHoldings().subtract(amount);
            return amount;
        } else {
            return Double.NaN;
        }
    }

    public boolean hasAmount(String name, double amount) {
        if (iconomy != null) {
            Account account = iConomy.getAccount(name);
            if (account == null) {
                return false;
            }
            return account.getHoldings().balance() >= amount;
        } else {
            return true;
        }
    }

    public String format(double amount) {
        if (iconomy != null) {
            return iConomy.format(amount);
        } else {
            return String.valueOf(amount);
        }
    }

    public void setIconomy(iConomy iconomy) {
        this.iconomy = iconomy;
    }
}