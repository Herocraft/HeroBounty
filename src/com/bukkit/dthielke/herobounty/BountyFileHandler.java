package com.bukkit.dthielke.herobounty;

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
