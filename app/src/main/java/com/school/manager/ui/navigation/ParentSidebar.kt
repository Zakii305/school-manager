package com.school.manager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val SidebarBg = Color(0xFF0B1730)
private val SidebarActive = Color(0xFF1E3A8A)
private val SidebarText = Color(0xFFCBD5E1)
private val SidebarActiveText = Color(0xFFFFFFFF)
private val SidebarIconDim = Color(0xFF94A3B8)
private val UserCapsule = Color(0xFF1E293B)
private val LogoutRed = Color(0xFFEF4444)

private data class ParentNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun ParentSidebarContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val branding by viewModel.branding.collectAsState()

    val items = listOf(
        ParentNavItem("My Children", Routes.PARENT_CHILDREN, Icons.Default.Groups),
        ParentNavItem("Messages", Routes.PARENT_MESSAGES, Icons.Default.ChatBubble)
    )

    ModalDrawerSheet(
        drawerContainerColor = SidebarBg,
        modifier = Modifier.width(290.dp)
    ) {
        Column(Modifier.fillMaxHeight()) {

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(branding.name,
                            color = Color.White, fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                        Text("PARENT PORTAL",
                            color = Color(0xFF94A3B8), fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = UserCapsule,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Signed in as",
                                color = Color(0xFF94A3B8), fontSize = 9.sp)
                            Text(user.name.ifBlank { "Parent" },
                                color = Color.White, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }

            Column(
                Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route ||
                        (currentRoute.startsWith(item.route) && item.route.length > 3)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) SidebarActive else Color.Transparent)
                            .clickable { onNavigate(item.route) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(item.icon, contentDescription = item.label,
                            tint = if (selected) SidebarActiveText else SidebarIconDim,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(item.label,
                            color = if (selected) SidebarActiveText else SidebarText,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold
                            else FontWeight.Normal)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().clickable { onLogout() }
                    .padding(horizontal = 26.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = LogoutRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Log Out", color = LogoutRed, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}
