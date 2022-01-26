import csv
import sys
import os


def read_csv(repos_file):
    if os.path.isfile(repos_file):
        with open(repos_file, newline='') as csv_file:
            lines = csv.reader(csv_file, delimiter=',', quotechar='|')
            return list(lines)
    else:
        return []


def git_action(main_repository, git_path, git_name, version):
    full_directory = '{}/{}'.format(main_repository, git_name)
    action = "set -x && git -C {} clone -b {} --depth 1 {}".format(main_repository, version, git_path)
    os.system(action)


def main(argv=None):
    main_repository = sys.argv[1]
    repos_file= sys.argv[2]
    version = sys.argv[3]
    dev = sys.argv[4]
    lines = read_csv(repos_file)

    if dev == "true":
        print("Cloning with dev")
        for row in lines:
            repo_type = row[0]
            git_path = row[1]
            git_name = row[2]

            if repo_type == "repos_own":
                git_action(main_repository+"/repos", git_path, git_name, version)
            elif repo_type == "repos_oca":
                git_action(main_repository+"/repos_oca", git_path, git_name, version[0:2]+".0")
            elif repo_type == "repos_extra":
                git_action(main_repository+"/repos_oca", git_path, git_name, version[0:2]+".0")

    else:
        print("Cloning without dev")
        for row in lines:
            repo_type = row[0]
            git_path = row[1]
            git_name = row[2]

            if repo_type == "repos_own":
                git_action(main_repository+"/repos", git_path, git_name, version)
            elif repo_type == "repos_oca":
                git_action(main_repository+"/repos_oca", git_path, git_name, version)
            elif repo_type == "repos_extra":
                git_action(main_repository+"/repos_oca", git_path, git_name, version)


if __name__ == "__main__":
    sys.exit(main())
