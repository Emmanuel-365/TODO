export interface User {
  id: string
  email: string
  name: string
}

export interface Task {
  id: string
  text: string
  done: boolean
  createdAt: string
}

export interface TodoList {
  id: string
  title: string
  tasks: Task[]
  createdAt: string
}
