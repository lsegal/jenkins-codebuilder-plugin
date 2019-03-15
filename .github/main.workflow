workflow "Build and Release" {
  on = "push"
  resolves = [
    "On Tag",
    "Build Plugin",
    "GitHub Release",
  ]
}

action "Build Plugin" {
  uses = "LucaFeger/action-maven-cli@9d8f23af091bd6f5f0c05c942630939b6e53ce44"
  args = "package"
}

action "On Tag" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "tag v*"
  needs = ["Build Plugin"]
}

action "GitHub Release" {
  uses = "./releasetools/hub-release"
  needs = ["On Tag"]
  secrets = ["RELEASE_TOKEN"]
}
