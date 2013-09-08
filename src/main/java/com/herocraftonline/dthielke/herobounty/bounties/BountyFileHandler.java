/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty.bounties;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class BountyFileHandler {
    @SuppressWarnings("unchecked")
    public static List<Bounty> load(File file) {
        Constructor constructor = new Constructor();
        constructor.addTypeDescription(new TypeDescription(Bounty.class, new Tag("bounty")));
        constructor.addTypeDescription(new TypeDescription(Point2D.class, new Tag("location")));

        Yaml yaml = new Yaml(constructor);

        try {
            List<Bounty> bounties = (List<Bounty>) yaml.load(new FileReader(file));

            if (bounties == null)
                return new ArrayList<Bounty>();
            return bounties;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void save(List<Bounty> bounties, File file) {
        Representer representer = new Representer();
        representer.addClassTag(Bounty.class, new Tag("bounty"));
        representer.addClassTag(Point2D.class, new Tag("location"));

        DumperOptions options = new DumperOptions();
        options.setWidth(300);
        options.setIndent(4);

        Yaml yaml = new Yaml(representer, options);

        try {
            yaml.dump(bounties, new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
