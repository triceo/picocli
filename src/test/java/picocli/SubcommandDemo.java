/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Demonstrates picocli subcommands.
 */
public class SubcommandDemo {

    @Command(name = "git", sortOptions = false, showDefaultValues = false,
            description = "Git is a fast, scalable, distributed revision control " +
                          "system with an unusually rich command set that provides both " +
                          "high-level operations and full access to internals.",
            commandListHeading = "%nCommands:%n%nThe most commonly used git commands are:%n")
    class Git {
        @Option(names = {"-V", "--version"}, help = true, description = "Prints version information and exits")
        boolean isVersionRequested;

        @Option(names = {"-h", "--help"}, help = true, description = "Prints this help message and exits")
        boolean isHelpRequested;

        @Option(names = "--git-dir", description = "Set the path to the repository")
        File gitDir;
    }

    // the "status" subcommand's has an option "mode" with a fixed number of values, modeled by this enum
    enum GitStatusMode {all, no, normal};

    @Command(name = "git-status",
            header = "Show the working tree status.",
            showDefaultValues = true,
            customSynopsis = "git-status [<options>...] [--] [<pathspec>...]",
            description = "Displays paths that have differences between the index file and the current HEAD commit, " +
                    "paths that have differences between the working tree and the index file, and paths in the " +
                    "working tree that are not tracked by Git (and are not ignored by gitignore(5)). The first " +
                    "are what you would commit by running git commit; the second and third are what you could " +
                    "commit by running git add before running git commit."
    )
    class GitStatus {
        @Option(names = {"-s", "--short"}, description = "Give the output in the short-format")
        boolean shortFormat;

        @Option(names = {"-b", "--branch"}, description = "Show the branch and tracking info even in short-format")
        boolean branchInfo;

        @Option(names = "--ignored", description = "Show ignored files as well") boolean showIgnored;

        @Option(names = {"-u", "--untracked"}, paramLabel = "<mode>", description = {
                "Show untracked files.",
                "The mode parameter is optional (defaults to `all`), and is used to specify the handling of untracked files.",
                "The possible options are:",
                " · no - Show no untracked files.",
                " · normal - Shows untracked files and directories.",
                " · all - Also shows individual files in untracked directories."
        })
        GitStatusMode mode = GitStatusMode.all;
    }

    @Command(name = "git-commit",
            sortOptions = false,
            headerHeading = "Usage:%n%n",
            synopsisHeading = "%n",
            descriptionHeading = "%nDescription:%n%n",
            parameterListHeading = "%nParameters:%n",
            optionListHeading = "%nOptions:%n",
            header = "Record changes to the repository.",
            description = "Stores the current contents of the index in a new commit " +
                    "along with a log message from the user describing the changes.")
    class GitCommit {
        @Option(names = {"-a", "--all"},
                description = "Tell the command to automatically stage files that have been modified " +
                        "and deleted, but new files you have not told Git about are not affected.")
        boolean all;

        @Option(names = {"-p", "--patch"}, description = "Use the interactive patch selection interface to chose which changes to commit")
        boolean patch;

        @Option(names = {"-C", "--reuse-message"}, paramLabel = "<commit>",
                description = "Take an existing commit object, and reuse the log message and the " +
                        "authorship information (including the timestamp) when creating the commit.")
        String reuseMessageCommit;

        @Option(names = {"-c", "--reedit-message"}, paramLabel = "<commit>",
                description = "Like -C, but with -c the editor is invoked, so that the user can" +
                        "further edit the commit message.")
        String reEditMessageCommit;

        @Option(names = "--fixup", paramLabel = "<commit>",
                description = "Construct a commit message for use with rebase --autosquash.")
        String fixupCommit;

        @Option(names = "--squash", paramLabel = "<commit>",
                description = " Construct a commit message for use with rebase --autosquash. The commit" +
                        "message subject line is taken from the specified commit with a prefix of " +
                        "\"squash! \". Can be used with additional commit message options (-m/-c/-C/-F).")
        String squashCommit;

        @Option(names = {"-F", "--file"}, paramLabel = "<file>",
                description = "Take the commit message from the given file. Use - to read the message from the standard input.")
        File file;

        @Option(names = {"-m", "--message"}, paramLabel = "<msg>",
                description = " Use the given <msg> as the commit message. If multiple -m options" +
                        " are given, their values are concatenated as separate paragraphs.")
        List<String> message = new ArrayList<String>();

        @Parameters(paramLabel = "<files>", description = "the files to commit")
        List<File> files = new ArrayList<File>();
    }

    // defines some commands to show in the list (option/parameters fields omitted for this demo)
    @Command(name = "git-add", header = "Add file contents to the index.") class GitAdd {}
    @Command(name = "git-branch", header = "List, create, or delete branches.") class GitBranch {}
    @Command(name = "git-checkout", header = "Checkout a branch or paths to the working tree.") class GitCheckout{}
    @Command(name = "git-clone", header = "Clone a repository into a new directory.") class GitClone{}
    @Command(name = "git-diff", header = "Show changes between commits, commit and working tree, etc.") class GitDiff{}
    @Command(name = "git-merge", header = "Join two or more development histories together.") class GitMerge{}
    @Command(name = "git-push", header = "Update remote refs along with associated objects.") class GitPush{}
    @Command(name = "git-rebase", header = "Forward-port local commits to the updated upstream head.") class GitRebase{}
    @Command(name = "git-tag", header = "Create, list, delete or verify a tag object signed with GPG.") class GitTag{}

    @Test
    public void testParseSubCommands() {
        CommandLine commandLine = new CommandLine(new Git());
        commandLine.addCommand("status", new GitStatus());
        commandLine.addCommand("commit", new GitCommit());
        commandLine.addCommand("add", new GitAdd());
        commandLine.addCommand("branch", new GitBranch());
        commandLine.addCommand("checkout", new GitCheckout());
        commandLine.addCommand("clone", new GitClone());
        commandLine.addCommand("diff", new GitDiff());
        commandLine.addCommand("merge", new GitMerge());
        commandLine.addCommand("push", new GitPush());
        commandLine.addCommand("rebase", new GitRebase());
        commandLine.addCommand("tag", new GitTag());

        List<Object> parsed = commandLine.parse("--git-dir=/home/rpopma/picocli status -sbuno".split(" "));
        assertEquals("command count", 2, parsed.size());

        assertEquals(Git.class, parsed.get(0).getClass());
        assertEquals(GitStatus.class, parsed.get(1).getClass());

        Git git = (Git) parsed.get(0);
        assertEquals(new File("/home/rpopma/picocli"), git.gitDir);

        GitStatus status = (GitStatus) parsed.get(1);
        assertTrue("status -s", status.shortFormat);
        assertTrue("status -b", status.branchInfo);
        assertFalse("NOT status --showIgnored", status.showIgnored);
        assertEquals("status -u=no", GitStatusMode.no, status.mode);
    }

    @Test
    public void testUsageMainCommand() throws Exception {
        CommandLine commandLine = new CommandLine(new Git());
        commandLine.addCommand("status", new GitStatus());
        commandLine.addCommand("commit", new GitCommit());
        commandLine.addCommand("add", new GitAdd());
        commandLine.addCommand("branch", new GitBranch());
        commandLine.addCommand("checkout", new GitCheckout());
        commandLine.addCommand("clone", new GitClone());
        commandLine.addCommand("diff", new GitDiff());
        commandLine.addCommand("merge", new GitMerge());
        commandLine.addCommand("push", new GitPush());
        commandLine.addCommand("rebase", new GitRebase());
        commandLine.addCommand("tag", new GitTag());

        String expected = "Usage: git [-hV] [--git-dir=<gitDir>]%n" +
                "Git is a fast, scalable, distributed revision control system with an unusually%n" +
                "rich command set that provides both high-level operations and full access to%n" +
                "internals.%n" +
                "  -V, --version               Prints version information and exits%n" +
                "  -h, --help                  Prints this help message and exits%n" +
                "      --git-dir=<gitDir>      Set the path to the repository%n" +
                "%n" +
                "Commands:%n" +
                "%n" +
                "The most commonly used git commands are:%n" +
                "  status    Show the working tree status.%n" +
                "  commit    Record changes to the repository.%n" +
                "  add       Add file contents to the index.%n" +
                "  branch    List, create, or delete branches.%n" +
                "  checkout  Checkout a branch or paths to the working tree.%n" +
                "  clone     Clone a repository into a new directory.%n" +
                "  diff      Show changes between commits, commit and working tree, etc.%n" +
                "  merge     Join two or more development histories together.%n" +
                "  push      Update remote refs along with associated objects.%n" +
                "  rebase    Forward-port local commits to the updated upstream head.%n" +
                "  tag       Create, list, delete or verify a tag object signed with GPG.%n";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        assertEquals(String.format(expected), result);
    }

    @Test
    public void testUsageSubCommandStatus() throws Exception {
        String expected = "Show the working tree status.%n" +
                "Usage: git-status [<options>...] [--] [<pathspec>...]%n" +
                "Displays paths that have differences between the index file and the current%n" +
                "HEAD commit, paths that have differences between the working tree and the index%n" +
                "file, and paths in the working tree that are not tracked by Git (and are not%n" +
                "ignored by gitignore(5)). The first are what you would commit by running git%n" +
                "commit; the second and third are what you could commit by running git add%n" +
                "before running git commit.%n" +
                "      --ignored               Show ignored files as well%n" +
                "  -b, --branch                Show the branch and tracking info even in%n" +
                "                                short-format%n" +
                "  -s, --short                 Give the output in the short-format%n" +
                "  -u, --untracked=<mode>      Show untracked files.%n" +
                "                              The mode parameter is optional (defaults to%n" +
                "                                `all`), and is used to specify the handling of%n" +
                "                                untracked files.%n" +
                "                              The possible options are:%n" +
                "                               · no - Show no untracked files.%n" +
                "                               · normal - Shows untracked files and directories.%n" +
                "                               · all - Also shows individual files in untracked%n" +
                "                                directories.%n" +
                "                              Default: all%n";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(new GitStatus(), new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        assertEquals(String.format(expected), result);
    }

    @Test
    public void testUsageSubCommandCommit() throws Exception {
        String expected = "Usage:%n" +
                "%n" +
                "Record changes to the repository.%n" +
                "%n" +
                "git-commit [-ap] [--fixup=<commit>] [--squash=<commit>] [-c=<commit>]%n" +
                "           [-C=<commit>] [-F=<file>] [-m[=<msg>...]] [<files>...]%n" +
                "%n" +
                "Description:%n" +
                "%n" +
                "Stores the current contents of the index in a new commit along with a log%n" +
                "message from the user describing the changes.%n" +
                "%n" +
                "Parameters:%n" +
                "      <files>                 the files to commit%n" +
                "%n" +
                "Options:%n" +
                "  -a, --all                   Tell the command to automatically stage files%n" +
                "                                that have been modified and deleted, but new%n" +
                "                                files you have not told Git about are not%n" +
                "                                affected.%n" +
                "  -p, --patch                 Use the interactive patch selection interface to%n" +
                "                                chose which changes to commit%n" +
                "  -C, --reuse-message=<commit>%n" +
                "                              Take an existing commit object, and reuse the log%n" +
                "                                message and the authorship information%n" +
                "                                (including the timestamp) when creating the%n" +
                "                                commit.%n" +
                "  -c, --reedit-message=<commit>%n" +
                "                              Like -C, but with -c the editor is invoked, so%n" +
                "                                that the user canfurther edit the commit%n" +
                "                                message.%n" +
                "      --fixup=<commit>        Construct a commit message for use with rebase%n" +
                "                                --autosquash.%n" +
                "      --squash=<commit>        Construct a commit message for use with rebase%n" +
                "                                --autosquash. The commitmessage subject line is%n" +
                "                                taken from the specified commit with a prefix%n" +
                "                                of \"squash! \". Can be used with additional%n" +
                "                                commit message options (-m/-c/-C/-F).%n" +
                "  -F, --file=<file>           Take the commit message from the given file. Use%n" +
                "                                - to read the message from the standard input.%n" +
                "  -m, --message[=<msg>...]     Use the given <msg> as the commit message. If%n" +
                "                                multiple -m options are given, their values are%n" +
                "                                concatenated as separate paragraphs.%n";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(new GitCommit(), new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        //System.out.println(result);
        assertEquals(String.format(expected), result);
    }
}
