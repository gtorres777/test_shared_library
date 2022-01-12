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


def git_action(main_oca_repository, git_path, git_name, version):
    full_directory = '{}/{}'.format(main_oca_repository, git_name)
    if os.path.isdir(full_directory):
        action = "set -x && git -C {} fetch origin && git -C {} reset --hard origin/{}".format(full_directory,
                                                                                               full_directory, version)
    else:
        action = "set -x && git -C {} clone -b {} --depth 1 {}".format(main_oca_repository, version, git_path)
    os.system(action)


def main(argv=None):
    main_extra_repository = sys.argv[1]
    repos_file= sys.argv[2]
    version = sys.argv[3]
    lines = read_csv(repos_file)
    for row in lines:
        git_path = row[0]
        git_name = row[1]
        git_action(main_extra_repository, git_path, git_name, version)


if __name__ == "__main__":
    sys.exit(main())
