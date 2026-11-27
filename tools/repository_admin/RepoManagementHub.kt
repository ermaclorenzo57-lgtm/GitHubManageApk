package com.lomipe.devhub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lomipe.devhub.core.*
import kotlinx.coroutines.CancellationException

private data class AdminAsk(val title: String,val detail: String,val work: suspend () -> Unit)

@Composable
fun RepoManagementHub(vm: AppViewModel, s: RepoSession) = key(vm.login, s.repo) {
    var section by remember { mutableStateOf("") }
    if (section == "General settings") {
        Column {
            TextButton(onClick={section=""}) { Text("All repository settings") }
            ManagePanel(vm,s)
        }
    } else {
        val login=vm.login
        var status by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf<AdminAsk?>(null) }
        val api = { RepoAdmin(vm.api().http,s.repo) }
        fun run(work: suspend () -> Unit) {
            vm.op {
                try {
                    check(vm.login == login && login != null)
                    work()
                    status="GitHub accepted the request."
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    status=if(e is ApiException) "GitHub rejected the request (HTTP "+e.code+"). Check permissions and repository rules." else "Unable to complete operation. Check inputs and reload."
                }
            }
        }
        fun ask(title:String, detail:String, work:suspend ()->Unit){confirm=AdminAsk(title,detail,work)}
        fun browser(path:String){Intents.openGitHubUrl(vm.ctx,"https://github.com/"+s.repo+path)}
        Page {
            MaxHeading("Repository settings",s.repo)
            Note("Administrative access depends on your token and GitHub policies. Lists show the first 100 results (discussions: 30).")
            if(status.isNotBlank()) Note(status)
            val ready=vm.busy==0
            if(section.isEmpty()){
                listOf("About repository","Profile","README editor","Branches","Branch protection","GitHub Pages","Collaborators","Variables","Secrets","Environments","Release manager","Fork and transfer","Discussions","Security","General settings").forEach { title ->
                    OutlinedButton(onClick={section=title;status=""},enabled=ready,modifier=Modifier.fillMaxWidth()){Text(title)}
                }
            } else key(section) {
                BtnO("All settings",ready){section="";status=""}
                H(section)
                var a by remember { mutableStateOf("") }
                var b by remember { mutableStateOf("") }
                var c by remember { mutableStateOf("") }
                var d by remember { mutableStateOf("") }
                var loaded by remember { mutableStateOf(false) }
                var flag by remember { mutableStateOf(false) }
                var rows by remember { mutableStateOf<List<JObj>>(emptyList()) }
                var info by remember { mutableStateOf<JObj?>(null) }
                var selected by remember { mutableStateOf<JObj?>(null) }
                when(section){
                    "About repository" -> {
                        BtnO("Load details",ready){run{val r=api().about();a=r.s("description").orEmpty();b=r.s("homepage").orEmpty();c=api().topics().joinToString(", ");loaded=true}}
                        if(loaded){
                            Field("Description",a,{a=it},false);Field("Website (HTTPS)",b,{b=it});Field("Topics (comma separated)",c,{c=it},false)
                            Btn("Save description and website",ready){val x=a;val y=b;ask("Update About?","Save the public-facing description and website."){api().updateAbout(x,y)}}
                            BtnO("Save topics",ready){val x=c;ask("Replace topics?","Replace the complete topic list."){api().updateTopics(x)}}
                        }
                    }
                    "Profile" -> {
                        BtnO("Load my profile",ready){run{val r=api().profile();a=r.s("name").orEmpty();b=r.s("bio").orEmpty();c=r.s("blog").orEmpty();loaded=true}}
                        if(loaded){
                            Field("Display name",a,{a=it});Field("Bio",b,{b=it},false);Field("Website",c,{c=it})
                            Btn("Save profile",ready){val x=a;val y=b;val z=c;ask("Update profile?","These fields may be publicly visible."){api().saveProfile(x,y,z)}}
                        }
                        BtnO("Open profile README repository",ready&&login!=null){login?.let{vm.pop();vm.openRepo(it+"/"+it)}}
                    }
                    "README editor" -> {
                        Note("Choose a branch. Protected branches need a pull request. The Files editor supports editing and diff review.")
                        Field("Branch",s.branch,{s.branch=it})
                        BtnO("Edit README.md",ready&&s.branch.isNotBlank()){s.subTab=1;s.openFile("README.md")}
                        BtnO("Create file",ready&&s.branch.isNotBlank()){s.subTab=1;s.newFileEditor()}
                        Field("Markdown draft",a,{a=it},false)
                        BtnO("Preview draft"){flag=!flag}
                        if(flag) MaxPanel {a.lines().forEach{line->if(line.startsWith("# ")) Text(line.drop(2),style=MaterialTheme.typography.headlineSmall) else if(line.startsWith("## ")) Text(line.drop(3),style=MaterialTheme.typography.titleMedium) else Text(line)}}
                        BtnO("Copy draft",a.isNotBlank()){Intents.copy(vm.ctx,"README draft",a)}
                    }
                    "Branches" -> {
                        BtnO("Load branches",ready){run{rows=api().branches()}}
                        rows.forEach{Note(it.s("name").orEmpty()+if(it.b("protected"))" (protected)" else "")}
                        Field("Source / base branch",a,{a=it});Field("New / head branch",b,{b=it})
                        BtnO("Compare",ready&&a.isNotBlank()&&b.isNotBlank()){val x=a;val y=b;run{info=api().compare(x,y)}}
                        info?.let{Note("Ahead "+it.l("ahead_by")+" · Behind "+it.l("behind_by")+" · "+it.s("status"))}
                        Btn("Create branch",ready&&a.isNotBlank()&&b.isNotBlank()){val x=a;val y=b;ask("Create branch?",y+" from "+x){api().createBranch(x,y);rows=api().branches()}}
                        Field("Pull request title",c,{c=it})
                        BtnO("Open merge pull request",ready&&a.isNotBlank()&&b.isNotBlank()&&c.isNotBlank()){val x=a;val y=b;val z=c;ask("Open pull request?",y+" into "+x){api().mergeRequest(x,y,z)}}
                        Field("Branch to delete",d,{d=it})
                        BtnO("Delete non-protected branch",ready&&d.isNotBlank()){val x=d;ask("Delete branch?",x+" will be deleted if permitted. Default and protected branches are refused."){api().deleteBranch(x);rows=api().branches()}}
                    }
                    "Branch protection" -> {
                        Field("Branch",a,{a=it})
                        BtnO("Inspect protection",ready&&a.isNotBlank()){val x=a;run{info=api().protection(x)}}
                        info?.let{p->Note("Rule found. Enforce admins: "+(p.o("enforce_admins")?.b("enabled")==true));Note("Required checks: "+p.o("required_status_checks")?.a("checks")?.mapNotNull{it.s("context")}?.joinToString(", ").orEmpty())}
                        BtnO("Configure full protection on GitHub"){browser("/settings/branches")}
                        Note("Full rule editing is intentionally delegated to GitHub to avoid dropping existing protections.")
                    }
                    "GitHub Pages" -> {
                        BtnO("Load Pages settings",ready){run{val p=api().pages();a=p.o("source")?.s("branch").orEmpty();b=p.o("source")?.s("path").orEmpty();loaded=true;info=p}}
                        info?.let{Note("Status: "+it.s("status")+" · "+it.s("html_url"))}
                        Field("Branch",a,{a=it});Field("Folder (/ or /docs)",b,{b=it})
                        Btn("Create branch-based site",ready&&a.isNotBlank()&&b in listOf("/","/docs")&&!loaded){val x=a;val y=b;ask("Create public site?","Files on the chosen branch may become publicly accessible."){api().savePages(x,y,true)}}
                        BtnO("Update existing site",ready&&loaded&&a.isNotBlank()&&b in listOf("/","/docs")){val x=a;val y=b;ask("Change Pages source?","Selected content will be deployed publicly."){api().savePages(x,y,false)}}
                        BtnO("Custom domain and advanced settings"){browser("/settings/pages")}
                    }
                    "Collaborators" -> {
                        BtnO("Load collaborators",ready){run{rows=api().collaborators()}}
                        rows.forEach{Note(it.s("login").orEmpty()+" · "+it.s("role_name").orEmpty())}
                        Field("GitHub login",a,{a=it});Field("Role (pull, triage, push, maintain, admin)",b,{b=it})
                        Btn("Invite / update access",ready&&a.isNotBlank()&&b in listOf("pull","triage","push","maintain","admin")){val x=a;val y=b;ask("Grant repository access?",x+" · "+y){api().invite(x,y)}}
                        BtnO("Remove collaborator",ready&&a.isNotBlank()&&!a.equals(login,true)){val x=a;ask("Remove collaborator?",x+" will lose direct access."){api().remove(x);rows=api().collaborators()}}
                    }
                    "Variables" -> {
                        Field("Environment (blank = repository)",a,{a=it})
                        BtnO("List variable names",ready){val x=a;run{rows=api().variables(x)}}
                        rows.forEach{Note(it.s("name").orEmpty())}
                        Field("Name",b,{b=it});Field("Value (not for credentials)",c,{c=it},false)
                        Row{Checkbox(flag,{flag=it});Text("Update existing variable")}
                        Btn("Save variable",ready&&b.isNotBlank()){val x=a;val y=b;val z=c;val u=flag;ask("Save non-secret variable?","Variable values are not secret. Never store credentials here."){api().saveVariable(x,y,z,u);c=""}}
                        BtnO("Delete variable",ready&&b.isNotBlank()){val x=a;val y=b;ask("Delete variable?",y+" may be used by workflows."){api().deleteVariable(x,y);rows=api().variables(x)}}
                    }
                    "Secrets" -> {
                        Field("Environment (blank = repository)",a,{a=it})
                        BtnO("List secret names",ready){val x=a;run{rows=api().secrets(x)}}
                        rows.forEach{Note(it.s("name").orEmpty())}
                        Note("Secret values are never retrieved or stored here. Create, rotate and delete encrypted secrets on GitHub.")
                        BtnO("Manage secrets on GitHub"){browser(if(a.isBlank())"/settings/secrets/actions" else "/settings/environments")}
                    }
                    "Environments" -> {
                        BtnO("Load environments",ready){run{rows=api().environments()}}
                        rows.forEach{Note(it.s("name").orEmpty())}
                        Field("Environment name",a,{a=it})
                        BtnO("Inspect environment",ready&&a.isNotBlank()){val x=a;run{info=api().environment(x)}}
                        info?.let{p->Note("Protection rules: "+p.a("protection_rules").mapNotNull{it.s("type")}.joinToString(", "));Note("Deployment policy: "+if(p.o("deployment_branch_policy")?.b("protected_branches")==true)"Protected branches" else if(p.o("deployment_branch_policy")?.b("custom_branch_policies")==true)"Selected branches" else "Check GitHub settings")}
                        BtnO("Create or edit environment protections"){browser("/settings/environments")}
                    }
                    "Release manager" -> {
                        BtnO("Load releases",ready){run{rows=api().releases()}}
                        rows.forEach{r->TextButton(onClick={selected=r}){Text(r.s("tag_name").orEmpty()+if(r.b("draft"))" (draft)" else "")}}
                        selected?.let{r->Note("Selected: "+r.s("tag_name")+" · assets "+r.a("assets").size);if(r.b("draft")){Field("Type the exact tag to confirm",a,{a=it});Btn("Publish selected draft",ready&&a==r.s("tag_name")){val id=r.l("id");val tag=a;ask("Publish release?",tag+" will become visible according to repository visibility. Verify assets and checksums first."){api().publishDraft(id,tag);rows=api().releases();selected=null}}}}
                        BtnO("Create draft and edit notes"){s.subTab=3;s.loadReleases()}
                        BtnO("Upload release assets"){s.subTab=0;s.mode="release"}
                    }
                    "Fork and transfer" -> {
                        Note("Use General settings to create a fork.")
                        Field("Fork branch to synchronize",a,{a=it})
                        Btn("Sync from upstream",ready&&a.isNotBlank()){val x=a;ask("Synchronize fork?",x+" may receive upstream changes."){api().syncFork(x)}}
                        BtnO("Transfer ownership on GitHub"){browser("/settings")}
                    }
                    "Discussions" -> {
                        BtnO("Load discussions",ready){run{info=api().discussions()}}
                        info?.let{r->
                            if(!r.b("hasDiscussionsEnabled")) Note("Discussions are not enabled.")
                            else{
                                r.o("discussionCategories")?.a("nodes")?.forEach{cat->TextButton(onClick={c=cat.s("id").orEmpty()}){Text("Category: "+cat.s("name"))}}
                                Field("Title",a,{a=it});Field("Message",b,{b=it},false)
                                Btn("Create discussion",ready&&a.isNotBlank()&&b.isNotBlank()&&c.isNotBlank()){val x=a;val y=b;val z=c;val id=r.s("id").orEmpty();ask("Post discussion?","Your content may be public."){api().createDiscussion(id,z,x,y);info=api().discussions()}}
                                r.o("discussions")?.a("nodes")?.forEach{item->TextButton(onClick={selected=item}){Text("#"+item.l("number")+" "+item.s("title"))}}
                                selected?.let{item->Note("Reply to "+item.s("title"));Field("Reply",d,{d=it},false);BtnO("Post reply",ready&&d.isNotBlank()){val x=d;val id=item.s("id").orEmpty();ask("Post reply?","Your reply may be public."){api().commentDiscussion(id,x);d=""}}}
                            }
                        }
                        BtnO("Open discussions on GitHub"){browser("/discussions")}
                    }
                    "Security" -> {
                        Note("Review GitHub's security results and configure supported free protection features. A green workflow is not proof of complete security.")
                        BtnO("Security alerts"){browser("/security")}
                        BtnO("Advanced Security settings"){browser("/settings/security_analysis")}
                        BtnO("Branch protections"){browser("/settings/branches")}
                    }
                }
            }
            confirm?.let{q->Confirm(q.title,q.detail,"Confirm",{confirm=null;run(q.work)},{confirm=null})}
        }
    }
}
